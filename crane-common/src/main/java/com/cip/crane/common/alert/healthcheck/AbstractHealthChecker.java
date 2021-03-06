package com.cip.crane.common.alert.healthcheck;

import com.cip.crane.common.utils.ClassLoaderUtils;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Author   mingdongli
 * 16/3/16  下午2:43.
 */
public abstract class AbstractHealthChecker implements HealthChecker, InitializingBean, DisposableBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private static final String ZK_CONF = "zooKeeper.properties";

    private static final String KEY_SESSION_TIMEOUT = "sessionTimeout";

    private static final int CONNECTION_TIMEOUT = 30 * 1000;

    private static final int RETRY_TIME = 4;

    private ZkClient zk;

    private ZkConnection zkConnection;

    @Override
    public void afterPropertiesSet() throws Exception {
        Properties props = new Properties();
        try {
            InputStream in = ClassLoaderUtils.getDefaultClassLoader().getResourceAsStream(ZK_CONF);
            props.load(in);
            in.close();
            String connectString = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty("taurus.zookeeper.connectstring");
            int sessionTimeout = Integer.parseInt(props.getProperty(KEY_SESSION_TIMEOUT));
            zkConnection = new ZkConnection(connectString, sessionTimeout);
            zk = new ZkClient(connectString, sessionTimeout, CONNECTION_TIMEOUT);
            zkConnection.connect(zk);
        } catch (Exception e) {
            logger.info("init zkclient error", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        zkConnection.close();
        zk.close();
    }

    @Override
    public boolean isHealthy() {
        String checkPath = getCheckPath();
        int retry = 0;
        while (retry < RETRY_TIME) {
            retry++;

            if (!existPath(checkPath)) {
                continue;
            } else {
                String leaderIp = getData(checkPath);
                if (StringUtils.isBlank(leaderIp)) {
                    continue;
                }
            }

            if (retry >= RETRY_TIME) {
                logger.info(String.format("Health check failed for path %s", getCheckPath()));
                return false;
            }
            return true;
        }

        return false;
    }

    protected String getData(String path) {

        try {
            byte[] data = zkConnection.getZookeeper().getData(path, false, null);
            try {
                return new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                logger.error(String.format("UnsupportedEncodingException path of %s", path), e1);
                return StringUtils.EMPTY;
            }
        } catch(KeeperException.SessionExpiredException e2){
            try {
                zkConnection.close();
                zkConnection.connect(zk);
            } catch (InterruptedException e) {
                logger.info("reconnec to zk failed");
            }
            return StringUtils.EMPTY;
        } catch (Exception e3) {
            logger.error(String.format("read data form path %s error", path), e3);
            return StringUtils.EMPTY;
        }

    }

    protected boolean existPath(String path) {
        return zk.exists(path);
    }

    protected abstract String getCheckPath();

}
