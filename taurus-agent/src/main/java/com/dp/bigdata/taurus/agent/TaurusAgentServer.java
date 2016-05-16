package com.dp.bigdata.taurus.agent;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dp.bigdata.taurus.agent.common.BaseEnvManager;
import com.dp.bigdata.taurus.agent.deploy.DeploymentUtility;
import com.dp.bigdata.taurus.agent.exec.Executor;
import com.dp.bigdata.taurus.agent.sheduler.ScheduleUtility;
import com.dp.bigdata.taurus.agent.spring.JarExecutor;
import com.dp.bigdata.taurus.agent.utils.AgentServerHelper;
import com.dp.bigdata.taurus.zookeeper.common.MachineType;
import com.dp.bigdata.taurus.zookeeper.common.infochannel.bean.HeartbeatInfo;
import com.dp.bigdata.taurus.zookeeper.common.infochannel.interfaces.DeploymentInfoChannel;
import com.dp.bigdata.taurus.zookeeper.common.infochannel.interfaces.ScheduleInfoChannel;
import com.google.inject.Inject;

public class TaurusAgentServer implements AgentServer{
    private static final Log LOGGER = LogFactory.getLog(TaurusAgentServer.class);

	private static final int CHECK_INTERVALS = 30*1000;

	private String localIp;
	private DeploymentInfoChannel deployer;
	private ScheduleInfoChannel scheduler;

	private int interval = CHECK_INTERVALS;
	private Executor executor;
	
	@Inject
	public TaurusAgentServer(DeploymentInfoChannel deployer,ScheduleInfoChannel scheduler, Executor executor, int interval){
	    this.deployer = deployer;
		this.scheduler = scheduler;
		this.executor = executor;

		localIp = AgentServerHelper.getLocalIp();
		if(interval > 0){
			this.interval = interval;
		}
	}

    private class AgentTask implements Runnable{

        public void run(){
            HeartbeatInfo info = new HeartbeatInfo();
            info.setTime(new Date());
            info.setAgentVersion(BaseEnvManager.AGENT_VERSION);
            info.setConfigs(BaseEnvManager.CONFIGS);
            info.setLinux(BaseEnvManager.ON_WINDOWS);
            info.setUser(BaseEnvManager.USER);
            info.setNeedUpdate(false);
            deployer.connectToCluster(MachineType.AGENT, localIp);
            scheduler.connectToCluster(MachineType.AGENT, localIp);


            DeploymentUtility.checkAndDeployTasks(localIp, deployer,true);
            DeploymentUtility.checkAndUndeployTasks( localIp, deployer,true);
            ScheduleUtility.checkAndRunTasks(executor, localIp, scheduler, true);
            ScheduleUtility.checkAndKillTasks(executor, localIp, scheduler, true);
            ScheduleUtility.checkAndOperate(executor, localIp, scheduler,true);
            ScheduleUtility.startZombieThread(localIp, scheduler);
            JarExecutor jarExecutor = new JarExecutor();
            jarExecutor.monitor();
            LOGGER.info("Taurus agent starts.");

            while(true){
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) { /*do nothing*/}
                DeploymentUtility.checkAndDeployTasks(localIp, deployer, false);
                DeploymentUtility.checkAndUndeployTasks(localIp, deployer, false);
                ScheduleUtility.checkAndRunTasks(executor, localIp, scheduler, false);
                ScheduleUtility.checkAndKillTasks(executor, localIp, scheduler, false);
                ScheduleUtility.checkAndOperate(executor, localIp, scheduler,false);
                scheduler.updateRealtimeHeartbeatInfo(MachineType.AGENT, localIp);
                HeartbeatInfo newHeartbeatInfo = scheduler.getHeartbeatInfo(MachineType.AGENT, localIp);
                if(newHeartbeatInfo != null&&newHeartbeatInfo.isNeedUpdate()){
                    info.setConfigs(newHeartbeatInfo.getConfigs());
                }
                scheduler.updateHeartbeatInfo(MachineType.AGENT, localIp, info);
                scheduler.updateRealtimeHeartbeatInfo(MachineType.AGENT, localIp);
            }
        }


    }

	public void start(){
        clearZombieJob(); //清除僵尸进程
        Thread agentThread = new Thread(new AgentTask());
        agentThread.setDaemon(true);
        agentThread.setName("Thread-Agent");
        agentThread.start();
	}

    public void clearZombieJob(){
        CommandLine cmdLine;
        cmdLine = new CommandLine("bash");
        cmdLine.addArgument("-c");
         cmdLine.addArgument(String.format(BaseEnvManager.clearZombie, BaseEnvManager.agentRoot, BaseEnvManager.agentRoot), false);
        try {
            executor.execute("clearZombieJob", 0, null, cmdLine, null, null);
        } catch (IOException e) {
            LOGGER.error("Clear Zombie Job ERROR, DETAIL INFO:" + e);
        }
    }
}
