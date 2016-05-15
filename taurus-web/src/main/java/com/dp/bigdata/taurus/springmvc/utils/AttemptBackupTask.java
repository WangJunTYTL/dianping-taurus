package com.dp.bigdata.taurus.springmvc.utils;

import com.dianping.cat.Cat;
import com.dp.bigdata.taurus.generated.mapper.AttemptBackupMapper;
import com.dp.bigdata.taurus.generated.module.Task;
import com.dp.bigdata.taurus.generated.module.TaskAttempt;
import com.dp.bigdata.taurus.generated.module.TaskAttemptExample;
import com.dp.bigdata.taurus.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Author   mingdongli
 * 16/5/10  下午6:32.
 */
@Component
public class AttemptBackupTask extends AbstractAttemptCleanTask {

    private static final int BATCH_SIZE = 1000;

    private Date previous;

    @Autowired
    private AttemptBackupMapper attemptBackupMapper;

    @Scheduled(cron = "0 */1 * * * ?")
    public void taskAttemptBackupExecute() {

        if (leaderElector.amILeader()) {
            backupDatabase();
        }

    }

    @Scheduled(cron = "0 */30 * * * ?")
    public void fixSizeRecord() {  //每30分钟执行一次

        if (lionValue && leaderElector.amILeader()) {
            int recordCount;
            Map<String, Task> registedTask = scheduler.getAllRegistedTask();
            for (String taskId : registedTask.keySet()) {
                int count = countOfTaskAttempt(taskId);
                if (count > getReserveRecord()) {
                    TaskAttempt taskAttempt = retrieveNthTaskAttempt(taskId, getReserveRecord());
                    if (taskAttempt != null) {
                        Date date = taskAttempt.getEndtime();
                        recordCount = attemptBackupMapper.deleteTaskAttempts(date, taskId);
                        System.out.println(recordCount);
                        Cat.logEvent(getClass().getSimpleName(), String.format("%s:%d", taskId, recordCount));

                    }
                }
            }
        }
    }

    private void backupDatabase() {

        Date stopDate = new Date();
        Date startDate;
        TaskAttempt firstTaskAttempt = retrieveLastBackupTaskAttempt();

        if (firstTaskAttempt == null) {
            startDate = DateUtils.NMonthAgo(getReserveMonth()).getTime();
            TaskAttempt taskAttempt = retrieveFirstTaskAttempt();
            if (taskAttempt == null) { //没有数据
                return;
            } else {
                Date initDate = taskAttempt.getEndtime();
                initDate = DateUtils.zeroMinute(initDate);
                if (initDate.after(startDate)) {
                    startDate = initDate;
                }
            }
        } else {
            startDate = firstTaskAttempt.getEndtime();
            if(startDate.equals(previous)){  //避免出现startDate为整点的死循环
                startDate = DateUtils.nextHalfHour(startDate);
            }
            startDate = DateUtils.zeroOrThirtyMinute(startDate);
            startDate = DateUtils.zeroSecond(startDate);
        }

        Date nextHalfHour = DateUtils.nextHalfHour(startDate);
        List<TaskAttempt> taskAttemptList = new ArrayList<TaskAttempt>();
        while (taskAttemptList != null && taskAttemptList.isEmpty() && nextHalfHour.before(stopDate)) { //防止中间某天没有数据
            taskAttemptList = taskAttemptMapper.getTaskAttempt(startDate, nextHalfHour);
            if (taskAttemptList != null && !taskAttemptList.isEmpty()) {
                int size = taskAttemptList.size();

                if (size <= BATCH_SIZE) {
                    attemptBackupMapper.batchiInsert(taskAttemptList);
                } else {
                    int times = 0;
                    int startIndex = times * BATCH_SIZE;
                    int stopIndex = (times + 1) * BATCH_SIZE;
                    while (stopIndex <= size) {
                        attemptBackupMapper.batchiInsert(taskAttemptList.subList(startIndex, stopIndex));
                        times++;
                        startIndex = times * BATCH_SIZE;
                        stopIndex = (times + 1) * BATCH_SIZE;
                    }
                    attemptBackupMapper.batchiInsert(taskAttemptList.subList(startIndex, size));
                }
                previous = startDate;
                Cat.logEvent(getClass().getSimpleName(), String.format("backup:%s:%d", startDate.toString(), taskAttemptList.size()));
                break;
            }
            startDate = nextHalfHour;
            nextHalfHour = DateUtils.nextHalfHour(nextHalfHour);
        }
    }

    @Override
    protected int doDeleteTaskAttempts(Date endTime) {
        int deleted = attemptBackupMapper.deleteTaskAttemptsByEndTime(endTime);
        int recordCount = 0;
        Map<String, Task> registedTask = scheduler.getAllRegistedTask();
        for (String taskId : registedTask.keySet()) {
            int count = countOfTaskAttempt(taskId);
            if (count > getReserveRecord()) {
                TaskAttempt taskAttempt = retrieveNthTaskAttempt(taskId, getReserveRecord());
                if (taskAttempt != null) {
                    Date date = taskAttempt.getEndtime();
                    recordCount = attemptBackupMapper.deleteTaskAttempts(date, taskId);
                    System.out.println(recordCount);
                }
            }
        }
        Cat.logEvent(getClass().getSimpleName(), String.format("%s:%d:%d", endTime.toString(), deleted, recordCount));
        return deleted;
    }

    private TaskAttempt retrieveTaskAttempt(String orderByClause) {
        TaskAttemptExample example = new TaskAttemptExample();
        example.or().andEndtimeIsNotNull();
        example.setOrderByClause(orderByClause);
        List<TaskAttempt> taskAttemptList = attemptBackupMapper.selectByExample(example);
        if (taskAttemptList != null && !taskAttemptList.isEmpty()) {
            return taskAttemptList.get(0);
        }
        return null;
    }

    protected int countOfTaskAttempt(String taskId) {
        TaskAttemptExample example = new TaskAttemptExample();
        example.or().andTaskidEqualTo(taskId);
        int count = attemptBackupMapper.countByExample(example);
        return count;
    }

    protected TaskAttempt retrieveNthTaskAttempt(String taskId, int N) {

        TaskAttemptExample example = new TaskAttemptExample();
        example.or().andTaskidEqualTo(taskId).andEndtimeIsNotNull();
        String orderByClause = "endTime desc limit " + N + ",1";
        example.setOrderByClause(orderByClause);
        List<TaskAttempt> taskAttemptList = attemptBackupMapper.selectByExample(example);
        if (taskAttemptList != null && !taskAttemptList.isEmpty()) {
            return taskAttemptList.get(0);
        }
        return null;
    }

    private TaskAttempt retrieveLastBackupTaskAttempt() {

        String orderByClause = "endTime desc limit 1";
        return retrieveTaskAttempt(orderByClause);
    }

    protected int getReserveMonth() {
        return 3;
    }

    protected int getReserveRecord() {
        return 300;
    }

}
