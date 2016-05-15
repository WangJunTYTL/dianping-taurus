package com.dp.bigdata.taurus.agent.sheduler;


import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;

import org.I0Itec.zkclient.IZkChildListener;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dp.bigdata.taurus.agent.common.BaseEnvManager;
import com.dp.bigdata.taurus.agent.common.TaskType;
import com.dp.bigdata.taurus.agent.exec.Executor;
import com.dp.bigdata.taurus.agent.utils.AgentServerHelper;
import com.dp.bigdata.taurus.agent.utils.LockHelper;

import com.dp.bigdata.taurus.common.zookeeper.infochannel.bean.ScheduleConf;
import com.dp.bigdata.taurus.common.zookeeper.infochannel.bean.ScheduleStatus;
import com.dp.bigdata.taurus.common.zookeeper.infochannel.interfaces.ScheduleInfoChannel;
import com.dp.bigdata.taurus.common.zookeeper.infochannel.interfaces.ScheduleInfoChannel.Operate;

public class ScheduleUtility {
	private static final Log LOGGER = LogFactory.getLog(ScheduleUtility.class);

	private static ExecutorService killThreadPool = AgentServerHelper.createThreadPool(2, 4);
	private static ExecutorService executeThreadPool = AgentServerHelper.createThreadPool(10, 10);	
	private static final String UPDATE_COMMAND = "source /etc/profile; %s/script/update-agent.sh > %s/agent-logs/update.log";
	private static final String RESTART_COMMAND = "source /etc/profile; %s/bin/start.sh";

	public static void checkAndKillTasks(Executor executor, String localIp, ScheduleInfoChannel cs, boolean addListener) {
	    synchronized (ScheduleUtility.class) {
    		LOGGER.debug("Start checkAndKillTasks");
    		if(addListener) {
                cs.setKillingJobListener(new TaskKillListener(executor, localIp, cs));
            } 
    		Set<String> currentNew = cs.getNewKillingJobInstanceIds(localIp);
    		for(String attemptID: currentNew){
    		    ScheduleConf conf = (ScheduleConf) cs.getConf(localIp, attemptID);
                if(conf != null && StringUtils.isNotBlank(conf.getTaskType()) && conf.getTaskType().equalsIgnoreCase(TaskType.SPRING.name())){
                    return;
                }
    			Runnable killThread = new KillTaskThread(executor, localIp, cs, attemptID);
    			killThreadPool.submit(killThread);
    		}
    		LOGGER.debug("End checkAndKillTasks");
	    }

	}

	public static void checkAndRunTasks(Executor executor, String localIp, ScheduleInfoChannel cs, boolean addListener) {
	    synchronized (ScheduleUtility.class) {
    		LOGGER.debug("Start checkAndRunTasks");
    		if(addListener) {
    		    cs.setExecutionJobListener(new TaskExcuteListener(executor, localIp, cs));
    		} 
    		Set<String> currentNew = cs.getNewExecutionJobInstanceIds(localIp);
    		for(String attemptID: currentNew){
    			submitTask(executor, localIp, cs, attemptID);
    		}
    		LOGGER.debug("End checkAndRunTasks");
	    }
	}
	
	public static void startZombieThread(String localIp, ScheduleInfoChannel cs) {
	    ZombieTaskThread thread = new ZombieTaskThread(localIp, cs);
	    new Thread(thread).start();
	}
	
	private static void submitTask(Executor executor, String localIp, ScheduleInfoChannel cs, String attemptID){
		Lock lock = LockHelper.getLock(attemptID);
		try{
			lock.lock();
			
			ScheduleConf conf = (ScheduleConf) cs.getConf(localIp, attemptID);
			if(conf != null && StringUtils.isNotBlank(conf.getTaskType()) && conf.getTaskType().equalsIgnoreCase(TaskType.SPRING.name())){
				return;
			}
			
			cs.completeExecution(localIp, attemptID);
			LOGGER.debug(attemptID + " start schedule");
			ScheduleStatus status = (ScheduleStatus) cs.getStatus(localIp, attemptID);

			if(status == null){
				LOGGER.error("status is null");
				return;
			} else if (status.getStatus() != ScheduleStatus.SCHEDULE_SUCCESS) {
				status.setStatus(ScheduleStatus.SCHEDULE_FAILED);
				cs.updateStatus(localIp, attemptID,status);
				return;
			}	
		} catch(Exception e) {
			LOGGER.error(e,e);
			ScheduleStatus status = new ScheduleStatus();
			status.setStatus(ScheduleStatus.SCHEDULE_FAILED);
			cs.updateStatus(localIp, attemptID,status);
		} finally{
			lock.unlock();
		}
		Runnable executeThread = new ExecuteTaskThread(executor, localIp, cs, attemptID);
		executeThreadPool.submit(executeThread);
		LOGGER.debug(attemptID + " end schedule");
	}

	private static abstract class BaseTaskListener implements IZkChildListener {

		protected String localIp;
		protected ScheduleInfoChannel cs;
		protected Executor executor;

		private BaseTaskListener(Executor executor, String localIp, ScheduleInfoChannel cs){
			this.localIp = localIp;
			this.cs = cs;
			this.executor = executor;
		}
	}

	private static final class TaskExcuteListener extends BaseTaskListener{
		private TaskExcuteListener(Executor executor, String localIp, ScheduleInfoChannel cs){
			super(executor, localIp, cs);
		}

        @Override
        public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
            checkAndRunTasks(executor, localIp, cs, false);    
        }
	}

	private static final class TaskKillListener extends BaseTaskListener{
		private TaskKillListener(Executor executor, String localIp, ScheduleInfoChannel cs){
			super(executor, localIp, cs);
		}

        @Override
        public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
            checkAndKillTasks(executor, localIp, cs, false);
        }
	}

    /**
     * @param executor
     * @param localIp
     * @param
     */
    public static void checkAndOperate(Executor executor, String localIp, ScheduleInfoChannel cs,boolean atStart) {
        synchronized (ScheduleUtility.class) {
        	for(Operate op:Operate.values()){
        		String opStr = op.name().toLowerCase();
				if(!cs.operateCompleted(localIp, opStr)){
                    if(atStart) {
                    	LOGGER.info(opStr + " " + localIp + " success");
                        cs.completeOperate(localIp, opStr);
                    } else {
                        try {
                            CommandLine cmdLine;
                            cmdLine = new CommandLine("bash");
                            cmdLine.addArgument("-c");
                            if(Operate.UPDATE.equals(op)){
                            	cmdLine.addArgument(String.format(UPDATE_COMMAND, BaseEnvManager.agentRoot, BaseEnvManager.agentRoot), false);
                            	executor.execute("updateAgent", 0, null, cmdLine, null, null);
                            } else if(Operate.RESTART.equals(op)){
                            	cmdLine.addArgument(String.format(RESTART_COMMAND, BaseEnvManager.agentRoot), false);
                            	executor.execute("restartAgent", 0, null, cmdLine, null, null);
                            } 
                        } catch (IOException e) {
                            LOGGER.error(e,e);
                        }
                    }
                }
        	}
        }
    }
}
