package com.dp.bigdata.taurus.zookeeper.execute.helper;

import com.dp.bigdata.taurus.zookeeper.common.MachineType;
import com.dp.bigdata.taurus.zookeeper.common.infochannel.bean.ScheduleConf;
import com.dp.bigdata.taurus.zookeeper.common.infochannel.bean.ScheduleStatus;
import com.dp.bigdata.taurus.zookeeper.common.infochannel.guice.ScheduleInfoChanelModule;
import com.dp.bigdata.taurus.zookeeper.common.infochannel.interfaces.ScheduleInfoChannel;
import com.dp.bigdata.taurus.zookeeper.common.utils.IPUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * DefaultExecutorManager
 * @author damon.zhu
 *
 */
public class DefaultExecutorManager implements ExecutorManager{
	
	private static final Log LOGGER = LogFactory.getLog(DefaultExecutorManager.class);
	private static final int DEFAULT_TIME_OUT_IN_SECONDS = 10;
	private static final int RETRY_SLEEP_TIME = 20*1000;           //20s
	private static Map<String, Lock> attemptIDToLockMap = new HashMap<String, Lock>();

	private ScheduleInfoChannel dic;
	private int opTimeout = DEFAULT_TIME_OUT_IN_SECONDS;
	
	public DefaultExecutorManager(){
		Injector injector = Guice.createInjector(new ScheduleInfoChanelModule());
		dic = injector.getInstance(ScheduleInfoChannel.class);
	    dic.connectToCluster(MachineType.SERVER,IPUtils.getFirstNoLoopbackIP4Address() );
	}
	
	private static Lock getLock(String attemptID){
		synchronized(attemptIDToLockMap){
			Lock lock = attemptIDToLockMap.get(attemptID);
			if(lock == null){
				lock = new ReentrantLock();
				attemptIDToLockMap.put(attemptID, lock);
			}
			return lock;
		}
	}
	
	private void executeInternal(ExecuteContext context)  throws ExecuteException {
	    String agentIP = context.getAgentIP();
        String taskID = context.getTaskID();
        String attemptID = context.getAttemptID();
        String cmd = context.getCommand();
        String taskType = context.getType();
        String proxyUser = context.getProxyUser();
        String taskUrl = context.getTaskUrl();
        Map<String,String> otherConfs = context.getExtendedConfs();
        
        ScheduleStatus status = (ScheduleStatus) dic.getStatus(agentIP, attemptID);
        if(status == null){
            ScheduleConf conf = new ScheduleConf();
            conf.setTaskID(taskID);
            conf.setAttemptID(attemptID);
            conf.setCommand(cmd);
            conf.setTaskType(taskType);
            conf.setUserName(proxyUser);
            conf.setTaskUrl(taskUrl);
            conf.setExtendedMap(otherConfs);
            status = new ScheduleStatus();
            status.setStatus(ScheduleStatus.SCHEDULE_SUCCESS);
            Lock lock = getLock(attemptID);
            try{
                lock.lock();
                dic.execute(agentIP, attemptID, conf, status);
            } catch (RuntimeException e) {
                LOGGER.error("Attempt "+attemptID + " schedule failed",e);
                status.setStatus(ScheduleStatus.SCHEDULE_FAILED);
                throw new ExecuteException(e);
            }   
            finally{
                lock.unlock();

            }
        }
        else{
            LOGGER.error("Attempt "+attemptID + " has already scheduled");
            throw new ExecuteException("Attempt "+attemptID + " has already scheduled");
        }
	}
	
    public void execute(ExecuteContext context) throws ExecuteException {
    	try{
    	    this.executeInternal(context);
    	} catch(ExecuteException e){
    	    //if get Agent unavailable exception, wait 20s and retry;
    	    if(e.getMessage().equals("Agent unavailable")) {
    	        try {
                    Thread.sleep(RETRY_SLEEP_TIME);
                } catch (InterruptedException e1) {
                    LOGGER.error(e1,e1);
                    throw e;
                }
                this.executeInternal(context);
    	    } else {
    	        throw e;
    	    }
    	}
    	
    }

    public void kill(ExecuteContext context) throws ExecuteException {
    	String agentIP = context.getAgentIP();
    	String attemptID = context.getAttemptID();
    	
    	ScheduleStatus status = new ScheduleStatus();
    	status = (ScheduleStatus) dic.getStatus(agentIP, attemptID);
        if(status == null || status.getStatus() != ScheduleStatus.EXECUTING) {
            LOGGER.error("Job Attempt:" + attemptID + " cannot be killed!");
            throw new ExecuteException("Job Attempt:" + attemptID + " cannot be killed!");
        } else{
            Lock lock = getLock(attemptID);
            try{
                lock.lock();
                Condition killFinish = lock.newCondition();
                ScheduleStatusListener listener = new ScheduleStatusListener(lock, killFinish, dic, agentIP, attemptID);
                dic.killTask(agentIP, attemptID, status, listener );
                
                if(!killFinish.await(opTimeout, TimeUnit.SECONDS)){
                    LOGGER.error("Delete " + attemptID + " timeout");
                    throw new ExecuteException("Delete " + attemptID + " timeout");
                }else{
                    status = listener.getScheduleStatus();
                }
                
                dic.completeKill(agentIP, attemptID, listener);
            } catch(InterruptedException e){
                LOGGER.error("Delete " + attemptID + " failed" ,e);
                throw new ExecuteException("Delete " + attemptID + " failed",e);
            }
            finally{
                lock.unlock();
            }
            if(status.getStatus()!=ScheduleStatus.DELETE_SUCCESS) {
                LOGGER.error("Delete " + attemptID + " failed");
                throw new ExecuteException("Delete " + attemptID + " failed");
            }
        }
    }

    public ExecuteStatus getStatus(ExecuteContext context) throws ExecuteException {
    	String agentIP = context.getAgentIP();
    	String attemptID = context.getAttemptID();

    	ScheduleStatus status = (ScheduleStatus) dic.getStatus(agentIP, attemptID);
        if(status == null) {
            //LOGGER.error("Fail to get status for attempt " +  agentIP + " : " + attemptID);
            throw new ExecuteException("Fail to get status");
        }
        ExecuteStatus result = null;
        int statusCode = status.getStatus();
        if(statusCode == ScheduleStatus.EXECUTE_FAILED) {
            result = new ExecuteStatus(ExecuteStatus.FAILED);
            dic.cleanupOnFinish(agentIP, attemptID);
        } else if(statusCode == ScheduleStatus.EXECUTE_SUCCESS) {
            result = new ExecuteStatus(ExecuteStatus.SUCCEEDED);
            dic.cleanupOnFinish(agentIP, attemptID);
        } else if(statusCode == ScheduleStatus.DELETE_SUCCESS) {
            result = new ExecuteStatus(ExecuteStatus.AUTO_KILLED);
            dic.cleanupOnFinish(agentIP, attemptID);
        } else if(statusCode == ScheduleStatus.UNKNOWN) {
            result = new ExecuteStatus(ExecuteStatus.UNKNOWN);
            dic.cleanupOnFinish(agentIP, attemptID);
        } else {
            result = new ExecuteStatus(ExecuteStatus.RUNNING);
        }

        result.setReturnCode(status.getReturnCode());
        return result;
    }


    private static final class ScheduleStatusListener implements IZkDataListener{

		private Condition scheduleFinish;
		private Lock lock;
		private ScheduleStatus status;
		private ScheduleInfoChannel dic;
		private String agentIp;
		private String attemptID;

		ScheduleStatusListener(Lock lock, Condition scheduleFinish, ScheduleInfoChannel cs, String agentIp,
				String attemptID){
			this.lock = lock;
			this.scheduleFinish = scheduleFinish;
			this.dic = cs;
			this.agentIp = agentIp;
			this.attemptID = attemptID;
		}
		
		public ScheduleStatus getScheduleStatus(){
			return status;
		}

        /* (non-Javadoc)
         * @see org.I0Itec.zkclient.IZkDataListener#handleDataChange(java.lang.String, java.lang.Object)
         */
        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {
            try{
                lock.lock();
                status = (ScheduleStatus) dic.getStatus(agentIp, attemptID);
                scheduleFinish.signal();
            } finally{
                lock.unlock();
            }
            
        }

        /* (non-Javadoc)
         * @see org.I0Itec.zkclient.IZkDataListener#handleDataDeleted(java.lang.String)
         */
        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            // TODO Auto-generated method stub
            
        }
	}


    /* (non-Javadoc)
     * @see com.dp.bigdata.taurus.zookeeper.execute.helper.ExecutorManager#registerNewHost()
     */
    @Override
    public List<String> registerNewHost() {
        // TODO Auto-generated method stub
        return null;
    }

}

