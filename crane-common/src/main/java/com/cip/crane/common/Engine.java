package com.cip.crane.common;

import com.cip.crane.common.alert.MailHelper;
import com.cip.crane.common.alert.OpsAlarmHelper;
import com.cip.crane.common.alert.WeChatHelper;
import com.cip.crane.common.execute.ExecuteStatus;
import com.cip.crane.common.execute.ExecutorManager;
import com.cip.crane.common.lion.ConfigHolder;
import com.cip.crane.common.lion.LionKeys;
import com.cip.crane.common.netty.MscheduleExecutorManager;
import com.cip.crane.common.structure.BoundedList;
import com.cip.crane.common.utils.SleepUtils;
import com.cip.crane.common.utils.ThreadUtils;
import com.cip.crane.generated.module.Host;
import com.cip.crane.generated.module.Task;
import com.cip.crane.zookeeper.common.heartbeat.AgentHandler;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.cip.crane.common.netty.zookeeper.ZookeeperManager;
import com.cip.crane.generated.mapper.HostMapper;
import com.cip.crane.generated.module.TaskAttempt;
import com.cip.crane.generated.module.TaskAttemptExample;
import com.cip.crane.common.execute.ExecuteException;
import com.cip.crane.zookeeper.common.heartbeat.AgentMonitor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Engine is the default implementation of the <code>Scheduler</code>.
 *
 * @author damon.zhu
 * @see Scheduler
 */
final public class Engine extends ListenableCachedScheduler implements Scheduler {

    @Autowired
    @Qualifier("triggle.crontab")
    private Triggle crontabTriggle;

    @Autowired
    @Qualifier("triggle.dependency")
    private Triggle dependencyTriggle;

    @Autowired
    @Qualifier("filter.isAllowMutilInstance")
    private Filter<List<String>> filter;

    @Autowired
    private TaskAssignPolicy assignPolicy;

    @Autowired
    private Selector<String> selector;

    @Autowired
    private IDFactory idFactory;

    @Autowired
    @Qualifier("proxy")
    private ExecutorManager executorManager;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private AgentMonitor agentMonitor;

    @Autowired(required = false)
    private ZookeeperManager zookeeperManager;

    private Runnable progressMonitor;

    private ExecutorService attemptExecutor = ThreadUtils.newFixedThreadPool(500, "AttemptExector");

    @Override
    public void afterPropertiesSet() throws Exception {

        super.afterPropertiesSet();
        crontabTriggle.registerAttemptListener(this);
        dependencyTriggle.registerAttemptListener(this);
        filter.registerAttemptListener(this);

    }

    public Engine() {
        super();
    }

    /**
     * start the engine;
     */
    public void start() {

        if (progressMonitor != null) {// spring注入了AttemptStatusMonitor
            Thread monitorThread = new Thread(progressMonitor);
            monitorThread.setName("Thread-" + AttemptStatusMonitor.class.getName());
            monitorThread.setDaemon(true);
            monitorThread.start();

        }

        Thread refreshThread = new RefreshThread();
        refreshThread.setDaemon(true);
        refreshThread.setName("Thread-" + RefreshThread.class.getName());
        refreshThread.start();

        Thread dependPassThread = new DependPassThread();
        dependPassThread.setDaemon(true);
        dependPassThread.setName("Thread-" + DependPassThread.class.getName());
        dependPassThread.start();

        Thread triggleThread = new TriggleTask();
        triggleThread.setDaemon(true);
        triggleThread.setName("Thread-" + TriggleTask.class.getName());
        triggleThread.start();

        agentMonitor.agentMonitor(new AgentHandler() {
            @Override
            public void disConnected(String ip) {
                Cat.logEvent("DisConnected", ip);
                try {

                    OpsAlarmHelper oaHelper = new OpsAlarmHelper();
                    String webDomain = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty("taurus.web.serverName");
                    String exceptContext = "您好，taurus-agent的job主机 ["
                            + ip
                            + "] 服务已经挂掉请重启，监控连接如下：" + webDomain + "/hosts?hostName="
                            + ip
                            + "，谢谢~";

                    String context = "您好，taurus-agent的job主机 ["
                            + ip
                            + "] 心跳异常，监控连接如下：" + webDomain + "/hosts?hostName="
                            + ip
                            + "，谢谢~";


                    String url1 = "http://" + ip + ":8080/agentrest.do?action=isnew";
                    String url2 = "http://" + ip + ":8088/agentrest.do?action=isnew";
                    String isAlive1 = get_data(url1);
                    String isAlive2 = get_data(url2);

                    String reportToOps = null;
                    try {
                        reportToOps = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty("taurus.agent.down.ops.report.alarm.post");
                    } catch (LionException e) {
                        reportToOps = "http://pulse.dp/report/alarm/post";
                        e.printStackTrace();
                    }

                    if ((isAlive1 != null && isAlive1.equals("true")) || (isAlive2 != null && isAlive2.equals("true"))) {
                        String admin = ConfigHolder.get(LionKeys.ADMIN_USER);
                        MailHelper.sendMail(admin + "@dianping.com", context, "Taurus-Agent主机心跳异常告警服务");
                        WeChatHelper.sendWeChat(admin, context, "Taurus-Agent主机心跳异常告警服务", ConfigHolder.get(LionKeys.ADMIN_WECHAT_AGENTID));


                        oaHelper.buildTypeObject("Taurus")
                                .buildTypeItem("Service")
                                .buildTypeAttribute("Status")
                                .buildSource("taurus")
                                .buildDomain(ip)
                                .buildTitle("Taurus-Agent主机心跳异常告警服务")
                                .buildContent(context)
                                .buildUrl(webDomain + "/hosts?hostName=" + ip)
                                .buildReceiver("dpop@dianping.com")
                                .sendAlarmPost(reportToOps);

                    } else {
                        String admin = ConfigHolder.get(LionKeys.ADMIN_USER);
                        WeChatHelper.sendWeChat(admin, exceptContext, "Taurus-Agent主机失联系告警服务", ConfigHolder.get(LionKeys.ADMIN_WECHAT_AGENTID));
                        String toMails = ConfigHolder.get(LionKeys.AGENT_DOWN_MAIL_TO);
                        String[] toLists = toMails.split(",");
                        for (String to : toLists) {
                            MailHelper.sendMail(to, exceptContext, "Taurus-Agent主机失联系告警服务");
                        }


                        oaHelper.buildTypeObject("Taurus")
                                .buildTypeItem("Service")
                                .buildTypeAttribute("Status")
                                .buildSource("taurus")
                                .buildDomain(ip)
                                .buildTitle("Taurus-Agent主机失联系告警服务")
                                .buildContent(exceptContext)
                                .buildUrl(webDomain + "/hosts?hostName=" + ip)
                                .buildReceiver("monitor@dianping.com")
                                .sendAlarmPost(reportToOps);

                    }

                } catch (Exception e) {
                    Cat.logError(e);
                }
                Host host = new Host();
                host.setName(ip);
                host.setIp(ip);
                host.setIsconnected(false);
                hostMapper.updateByPrimaryKeySelective(host);
            }

            @Override
            public void connected(String ip) {
                Cat.logEvent("Connected", ip);

                Host host = hostMapper.selectByPrimaryKey(ip);
                Host newHost = new Host();
                newHost.setIp(ip);
                newHost.setName(ip);
                newHost.setIsconnected(true);
                newHost.setIsonline(true);
                if (host == null) {
                    newHost.setPoolid(1);
                    hostMapper.insert(newHost);
                } else {
                    hostMapper.updateByPrimaryKeySelective(newHost);
                }
            }

            @Override
            public List<String> getConnectedFromDB() {
                List<Host> hosts = hostMapper.selectByExample(null);
                List<String> result = new ArrayList<String>();
                for (Host host : hosts) {
                    if (host.getIsconnected() == true) {
                        result.add(host.getIp());
                    }
                }
                return result;
            }
        });

    }

    class TriggleTask extends Thread {
        @Override
        public void run() {
            while (true) {

                while (interrupted.get()) {
                    triggleThreadRestFlag = true;
                    SleepUtils.sleep(5000);
                }
                triggleThreadRestFlag = false;

                Transaction t = Cat.newTransaction("Engine", "Schedule");
                try {


                    Transaction cron = Cat.newTransaction("Engine", "Cron");
                    crontabTriggle.triggle();
                    cron.setStatus(Message.SUCCESS);
                    cron.complete();

                    Transaction depend = Cat.newTransaction("Engine", "Depend");
                    dependencyTriggle.triggle(CollectionUtils.union(attemptsOfStatusInitialized, attemptsOfStatusDependTimeout));
                    depend.setStatus(Message.SUCCESS);
                    depend.complete();

                    Transaction fil = Cat.newTransaction("Engine", "Filter");
                    List<AttemptContext> contexts = filter.filter(getReadyToRunAttempt());

                    if (contexts != null) {
                        for (final AttemptContext context : contexts) {
                            removeDependPassAttempt(context.getAttempt()); //已调度的从缓存删除
                            attemptExecutor.execute(new AttemptTask(context));
                        }
                    }
                    fil.setStatus(Message.SUCCESS);
                    fil.complete();

                    t.setStatus(Message.SUCCESS);
                } catch (Throwable e) {
                    Cat.logError(e);
                    logger.error(e);
                } finally {
                    t.complete();
                }

                try {
                    Thread.sleep(SCHDUELE_INTERVAL);
                } catch (InterruptedException e) {
                    logger.error("Interrupted exception", e);
                }

            }
        }
    }

    class AttemptTask extends Thread {
        AttemptContext context;

        public AttemptTask(AttemptContext context) {
            this.context = context;
        }

        @Override
        public void run() {
            try {
                executeAttempt(context);
            } catch (ScheduleException se) {
                Cat.logError("fail to schedule the attempt : " + context.getAttemptid(), se);
            }
        }
    }

    class RefreshThread extends Thread {

        @Override
        public void run() {


            while (true) {

                while (interrupted.get()) {
                    refreshThreadRestFlag = true;
                    SleepUtils.sleep(5000);
                }
                refreshThreadRestFlag = false;

                try {
                    load();

                    Thread.sleep(20 * 1000);

                } catch (InterruptedException e) {
                    logger.error("RefreshThread was interrupted!", e);
                }

            }


        }

    }

    @Override
    public synchronized void registerTask(Task task) throws ScheduleException {
        if (!registedTasks.containsKey(task.getTaskid())) {
            taskMapper.insertSelective(task);
            task = taskMapper.selectByPrimaryKey(task.getTaskid());
            registedTasks.put(task.getTaskid(), task);
            tasksMapCache.put(task.getName(), task.getTaskid());
            addOrUpdateCronCache(task);

            Cat.logEvent("Task-Create", task.getName());
        } else {
            throw new ScheduleException("The task : " + task.getTaskid() + " has been registered.");
        }
    }

    @Override
    public synchronized void unRegisterTask(String taskID) throws ScheduleException {
        Map<String, AttemptContext> contexts = runningAttempts.get(taskID);
        if (contexts != null && contexts.size() > 0) {
            throw new ScheduleException("There are running attempts, so cannot remove this task");
        }

        if (registedTasks.containsKey(taskID)) {
            Task task = registedTasks.get(taskID);
            task.setStatus(TaskStatus.DELETED);
            taskMapper.updateByPrimaryKeySelective(task);
            registedTasks.remove(taskID);
            tasksMapCache.remove(task.getName());
            removeTaskAttempt(taskID, true);
            registeredCron.remove(taskID);

            Cat.logEvent("Task-Delete", task.getName());
        }
    }

    @Override
    public synchronized void updateTask(Task task) throws ScheduleException {
        if (registedTasks.containsKey(task.getTaskid())) {
            Task origin = registedTasks.get(task.getTaskid());
            task.setUpdatetime(new Date());
            task.setStatus(origin.getStatus());
            task.setCreator(null);
            if (StringUtils.isEmpty(task.getAppname())) {
                task.setAppname(origin.getAppname());
            }
            taskMapper.updateByPrimaryKeySelective(task);
            registedTasks.remove(task.getTaskid());
            Task tmp = taskMapper.selectByPrimaryKey(task.getTaskid());
            registedTasks.put(task.getTaskid(), tmp);
            addOrUpdateCronCache(task);

            Cat.logEvent("Task-Update", task.getName());
        } else {
            throw new ScheduleException("The task : " + task.getTaskid() + " has not been found.");
        }
    }

    @Override
    public synchronized void executeTask(String taskID, long timeout) throws ScheduleException {
        String instanceID = idFactory.newInstanceID(taskID);
        TaskAttempt attempt = new TaskAttempt();
        String attemptID = idFactory.newAttemptID(instanceID);
        attempt.setInstanceid(instanceID);
        attempt.setTaskid(taskID);
        attempt.setStatus(AttemptStatus.DEPENDENCY_PASS);
        attempt.setAttemptid(attemptID);
        attempt.setScheduletime(new Date());
        taskAttemptMapper.insertSelective(attempt);
        Task task = registedTasks.get(taskID);
        AttemptContext context = new AttemptContext(attempt, task);
        attemptExecutor.submit(new AttemptTask(context));
    }

    @Override
    public synchronized void suspendTask(String taskID) throws ScheduleException {
        if (registedTasks.containsKey(taskID)) {
            Task task = registedTasks.get(taskID);
            task.setStatus(TaskStatus.SUSPEND);
            task.setUpdatetime(new Date());
            taskMapper.updateByPrimaryKeySelective(task);
            removeTaskAttempt(taskID, false); //继续调度pass的job

            Cat.logEvent("Task-Suspend", task.getName());
        } else {
            throw new ScheduleException("The task : " + taskID + " has not been found.");
        }
    }

    @Override
    public void resumeTask(String taskID) throws ScheduleException {
        if (registedTasks.containsKey(taskID)) {
            Task task = registedTasks.get(taskID);
            task.setStatus(TaskStatus.RUNNING);
            Date current = new Date();
            task.setLastscheduletime(current);
            task.setUpdatetime(current);
            taskMapper.updateByPrimaryKeySelective(task);
            loadTaskAttempt(taskID);

            Cat.logEvent("Task-Resume", task.getName());
        } else {
            throw new ScheduleException("The task : " + taskID + " has not been found.");
        }
    }

    public void executeAttempt(AttemptContext context) throws ScheduleException {
        TaskAttempt attempt = context.getAttempt();
        String attemptId = context.getAttemptid();
        Task task = context.getTask();
        Host host;
        if (task.getPoolid() == 1) {
            host = new Host();
            String ip = task.getHostname();
            Collection<String> ipCollection;
            // assume that hostname is ip address!!
            if (StringUtils.isBlank(ip)) {
                ipCollection = zookeeperManager.getTaskNodes(task.getTaskid());
                if (ipCollection == null) {
                    Cat.logEvent("Attempt.SubmitFailed", context.getName(), "no-host", attemptId);
                    failAttempt(attempt);
                    throw new ScheduleException("Fail to execute attemptID : " + attemptId + " due to no host");
                }
            } else {
                if (MscheduleExecutorManager.MSCHEDULE_TYPE.equals(context.getType())) {
                    String[] ipAndPort = ip.split(",");
                    Set<String> tmpCollection = zookeeperManager.getTaskNodes(task.getTaskid());
                    if (tmpCollection == null) {
                        Cat.logEvent("Attempt.SubmitFailed", context.getName(), "no-host", attemptId);
                        failAttempt(attempt);
                        throw new ScheduleException("Fail to execute attemptID : " + attemptId + " due to no host");
                    }
                    ipCollection = CollectionUtils.intersection(Arrays.asList(ipAndPort), tmpCollection);
                } else {
                    ipCollection = Arrays.asList(ip.split(","));
                }
            }
            String finalIp = selector.select(ipCollection);
            host.setIp(finalIp);
            // host = hostMapper.selectByPrimaryKey(task.getHostname());
        } else {
            host = assignPolicy.assignTask(task);
        }
        attempt.setExechost(host.getIp());
        Date date = new Date();
        attempt.setStarttime(date);

        try {
            executorManager.execute(context.getContext());
            attempt.setStatus(AttemptStatus.RUNNING);
            int rows = taskAttemptMapper.updateStatusToRunning(attempt);
            if(rows == 0){
                attempt.setStatus(null);
                taskAttemptMapper.updateByPrimaryKeySelective(attempt);
                Cat.logEvent("Mschedule", attemptId + ":" + date.toString());
            }
            logger.info("Attempt " + attemptId + " is running now...");
            Cat.logEvent("Attempt.Scheduled", context.getName(), Message.SUCCESS, attemptId);
        } catch (Exception ee) {
            Cat.logError(ee);
            Cat.logEvent("Attempt.SubmitFailed", context.getName(), "submit-fail", attemptId);

            failAttempt(attempt);

            throw new ScheduleException("Fail to execute attemptID : " + attemptId + " on host : "
                    + host.getIp(), ee);
        }

        // register the attempt context
        registAttemptContext(context);
    }

    private void failAttempt(TaskAttempt attempt) {

        attempt.setStatus(AttemptStatus.SUBMIT_FAIL);
        attempt.setEndtime(new Date());
        taskAttemptMapper.updateByPrimaryKeySelective(attempt);
    }

    @Override
    public synchronized void killAttempt(String attemptID) throws ScheduleException {
        HashMap<String, AttemptContext> contexts = runningAttempts.get(AttemptID.getTaskID(attemptID));
        AttemptContext context = contexts.get(attemptID);
        if (context == null) {
            throw new ScheduleException("Unable find attemptID : " + attemptID);
        }
        try {
            executorManager.kill(context.getContext());
        } catch (Exception ee) {
            logger.error("Fail to kill attemptID :  " + attemptID + " on host : " + context.getExechost());
        }

        context.getAttempt().setStatus(AttemptStatus.AUTO_KILLED);
        context.getAttempt().setEndtime(new Date());
        context.getAttempt().setReturnvalue(-1);
        taskAttemptMapper.updateByPrimaryKeySelective(context.getAttempt());
        unregistAttemptContext(context.getTaskid(), attemptID);

        Cat.logEvent("Kill-Attempt", context.getName(), Message.SUCCESS, context.getAttemptid());
    }

    @Override
    public synchronized void killAttemptManual(String attemptID) throws ScheduleException {
        HashMap<String, AttemptContext> contexts = runningAttempts.get(AttemptID.getTaskID(attemptID));
        AttemptContext context = contexts.get(attemptID);
        if (context == null) {
            throw new ScheduleException("Unable find attemptID : " + attemptID);
        }
        try {
            executorManager.kill(context.getContext());
        } catch (Exception ee) {
            logger.error("Fail to kill attemptID :  " + attemptID + " on host : " + context.getExechost());
        }

        context.getAttempt().setStatus(AttemptStatus.MAN_KILLED);
        context.getAttempt().setEndtime(new Date());
        context.getAttempt().setReturnvalue(-1);
        taskAttemptMapper.updateByPrimaryKeySelective(context.getAttempt());
        unregistAttemptContext(context.getTaskid(), attemptID);

        Cat.logEvent("Kill-Attempt", context.getName(), Message.SUCCESS, context.getAttemptid());
    }

    @Override
    public void attemptSucceed(String attemptID) {

        TaskAttempt attempt = new TaskAttempt();

        attempt.setAttemptid(attemptID);
        attempt.setReturnvalue(0);
        attempt.setEndtime(new Date());
        attempt.setStatus(AttemptStatus.SUCCEEDED);
        taskAttemptMapper.updateByPrimaryKeySelective(attempt);
        logger.info(attemptID + ":" + taskAttemptMapper.selectByPrimaryKey(attemptID).getStatus());

        Cat.logEvent("Attempt-Succeed", attemptID);
        unregistAttemptContext(AttemptID.getTaskID(attemptID), attemptID);
    }

    @Override
    public void attemptExpired(String attemptID) {
        AttemptContext context = runningAttempts.get(AttemptID.getTaskID(attemptID)).get(attemptID);
        TaskAttempt attempt = context.getAttempt();
        attempt.setEndtime(new Date());
        attempt.setStatus(AttemptStatus.TIMEOUT);
        taskAttemptMapper.updateByPrimaryKeySelective(attempt);

        Cat.logEvent("Attempt-Expired", context.getName(), Message.SUCCESS, context.getAttemptid());
    }

    @Override
    public void attemptFailed(String attemptID) {
        AttemptContext context = runningAttempts.get(AttemptID.getTaskID(attemptID)).get(attemptID);
        TaskAttempt attempt;

        if (context != null) {
            attempt = context.getAttempt();
        } else { //netty通信方式在重启时会丢失未确认的attempt
            attempt = taskAttemptMapper.selectByPrimaryKey(attemptID);
            Cat.logEvent("NullAttempt", attemptID);
        }
        attempt.setStatus(AttemptStatus.FAILED);
        attempt.setEndtime(new Date());
        taskAttemptMapper.updateByPrimaryKeySelective(attempt);

        unregistAttemptContext(attempt.getTaskid(), attemptID);
        Task task;

        if (context != null) {
            Cat.logEvent("Attempt-Failed", context.getName(), Message.SUCCESS, context.getAttemptid());
            task = context.getTask();
        } else {
            String taskId = attempt.getTaskid();
            task = taskMapper.selectByPrimaryKey(taskId);
        }

		/*
         * Check whether it is necessary to retry this failed attempt. If true, insert new attempt into the database; Otherwise, do
		 * nothing.
		 */
        if (task.getIsautoretry()) {
            TaskAttemptExample example = new TaskAttemptExample();
            example.or().andInstanceidEqualTo(attempt.getInstanceid());
            List<TaskAttempt> attemptsOfRecentInstance = taskAttemptMapper.selectByExample(example);
            if (task.getRetrytimes() < attemptsOfRecentInstance.size() - 1) {
                // do nothing
            } else if (task.getRetrytimes() == attemptsOfRecentInstance.size() - 1) {
                // do nothing
            } else {
                Cat.logEvent("Attempt-Expired-Retry", task.getName(), Message.SUCCESS, attemptID);

                logger.info("Attempt " + attempt.getAttemptid() + " fail, begin to retry the attempt...");
                String instanceID = attempt.getInstanceid();
                TaskAttempt retry = new TaskAttempt();
                String id = idFactory.newAttemptID(instanceID);
                retry.setAttemptid(id);
                retry.setTaskid(task.getTaskid());
                retry.setInstanceid(instanceID);
                retry.setScheduletime(attempt.getScheduletime());
                retry.setStatus(AttemptStatus.DEPENDENCY_PASS);
                taskAttemptMapper.insertSelective(retry);
                addDependPassAttempt(retry);
            }
        }
    }

    public void attemptUnKnown(String attemptID) {

        TaskAttempt attempt = new TaskAttempt();

        attempt.setAttemptid(attemptID);
        attempt.setEndtime(new Date());
        attempt.setStatus(AttemptStatus.UNKNOWN);
        attempt.setReturnvalue(-1);
        taskAttemptMapper.updateByPrimaryKeySelective(attempt);
        String taskId = AttemptID.getTaskID(attemptID);
        unregistAttemptContext(taskId, attemptID);

        Cat.logEvent("Attempt-Unknown", taskId, Message.SUCCESS, attemptID);

    }

    @Override
    public AttemptStatus getAttemptStatus(String attemptID) {
        HashMap<String, AttemptContext> maps = runningAttempts.get(AttemptID.getTaskID(attemptID));
        AttemptContext context = maps.get(attemptID);
        ExecuteStatus status = null;
        try {
            status = executorManager.getStatus(context.getContext());
        } catch (ExecuteException ee) {
            // 当心跳节点消失后出现异常，但是作业仍应该是running状态。
            status = new ExecuteStatus(AttemptStatus.RUNNING);
        }
        AttemptStatus astatus = new AttemptStatus(status.getStatus());
        astatus.setReturnCode(status.getReturnCode());
        return astatus;
    }

    private List<AttemptContext> getReadyToRunAttempt() {
        List<AttemptContext> contexts = new ArrayList<AttemptContext>();
        List<TaskAttempt> attempts = new ArrayList<TaskAttempt>();
        for (Map.Entry<String, BoundedList<TaskAttempt>> entry : dependPassMap.entrySet()) {
            BoundedList<TaskAttempt> tmpAttempts = entry.getValue();
            if (tmpAttempts != null && tmpAttempts.size() > 0) {
                if (tmpAttempts.size() > 1) {
                    Collections.sort(tmpAttempts, new TaskAttemptComparator());
                }
                attempts.add(tmpAttempts.get(0));  //只取第一个, 不取全部
            }
        }
        Collections.sort(attempts, new TaskAttemptComparator());
        for (TaskAttempt attempt : attempts) {
            Task task = registedTasks.get(attempt.getTaskid());
            if (task != null) {
                contexts.add(new AttemptContext(attempt, task));
            }
        }
        return contexts;
    }

    private synchronized void registAttemptContext(AttemptContext context) {
        HashMap<String, AttemptContext> contexts = runningAttempts.get(context.getTaskid());
        if (contexts == null) {
            contexts = new HashMap<String, AttemptContext>();
        }
        contexts.put(context.getAttemptid(), context);
        runningAttempts.put(context.getTaskid(), contexts);
    }

    private void unregistAttemptContext(String taskId, String attemptId) {
        if (runningAttempts.containsKey(taskId)) {
            HashMap<String, AttemptContext> contexts = runningAttempts.get(taskId);
            if (contexts.containsKey(attemptId)) {
                contexts.remove(attemptId);
            }
        }
    }

    public void setProgressMonitor(Runnable progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    @Override
    public TaskAttempt getRecentFiredAttemptByTaskID(String taskID) {
        TaskAttemptExample example = new TaskAttemptExample();

        example.or().andTaskidEqualTo(taskID);
        example.setOrderByClause("scheduleTime desc limit 1");

        List<TaskAttempt> attempts = taskAttemptMapper.selectByExample(example);

        if (attempts != null && attempts.size() == 1) {
            return attempts.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void expireCongestionAttempt(String attemptID) {
        // Auto-generated method stub
        TaskAttemptExample example = new TaskAttemptExample();
        example.or().andAttemptidEqualTo(attemptID);
        List<TaskAttempt> attempts = taskAttemptMapper.selectByExample(example);

        TaskAttempt attempt = attempts.get(0);

        attempt.setStatus(AttemptStatus.CONGESTION_SKIPED);
        attempt.setEndtime(new Date());
        taskAttemptMapper.updateByPrimaryKeySelective(attempt);

        removeDependPassAttempt(attempt);

        Cat.logEvent("Congestion-Expire-Attempt", attempt.getTaskid(), Message.SUCCESS, attempt.getAttemptid());
    }

    public static String get_data(String url) {
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(1000);
            conn.connect();

            //返回
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = reader.readLine();
            return result.trim();
        } catch (Exception e) {

            return null;
        }
    }

    /**
     * graceful shutdown the server;
     */
    public void stop() {
    }

    public void isInterrupt(boolean interrupt) {
        boolean current = interrupted.get();
        interrupted.compareAndSet(current, interrupt);
        ((AttemptStatusMonitor) progressMonitor).isInterrupt(interrupt);
        agentMonitor.interruptMonitor(interrupt);
    }

    @Override
    protected Filter<List<String>> getFilter() {
        return filter;
    }

    public static void main(String[] args) {
        Set<String> set = new HashSet<String>();
        set.add("123456");
        set.add("123");
        Collection<String> collection = set;
        String[] array = new String[]{"123456"};
        System.out.println(collection);
        System.out.println(CollectionUtils.intersection(set, Arrays.asList(array)));
    }
}
