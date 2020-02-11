package com.hyr.quartz.listener;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * @date 2018-11-11 下午 11:11
 * @author: <a href=mailto:huangyr@bonree.com>黄跃然</a>
 * @Description: SchedulerListener 调度器监听
 ******************************************************************************/
public class DefaultSchedulerListener implements SchedulerListener {

    private static Logger log = LoggerFactory.getLogger(DefaultSchedulerListener.class);

    private String name; // 监听器名称

    public DefaultSchedulerListener(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void jobAdded(JobDetail job) {
        String jobName = job.getKey().getName();
        log.info(getName() + " - the job:{} is add.", jobName);
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        String jobName = jobKey.getName();
        log.info(getName() + " - the job:{} is delete.", jobName);
    }

    @Override
    public void jobPaused(JobKey jobKey) {
        String jobName = jobKey.getName();
        log.info(getName() + " - the job:{} is pause.", jobName);
    }

    @Override
    public void jobResumed(JobKey jobKey) {
        String jobName = jobKey.getName();
        log.info(getName() + " - the job:{} is resume.", jobName);
    }

    /**
     * Scheduler 在有新的 JobDetail 部署时调用这两个中的相应方法。
     *
     * @param trigger
     */
    @Override
    public void jobScheduled(Trigger trigger) {
        String triggerName = trigger.getKey().getName();
        String jobName = trigger.getJobKey().getName();
        log.info(getName() + " - the job:{} is scheduled. triggerName：{}", jobName, triggerName);
    }

    /**
     * Scheduler 在有新的 JobDetail 卸载时调用这两个中的相应方法。
     *
     * @param triggerKey
     */
    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        String triggerName = triggerKey.getName();
        log.info(getName() + " - have job unscheduled. triggerName：{}", triggerName);
    }

    @Override
    public void jobsPaused(String group) {
        log.info(getName() + " - the group job is paused. group:{}", group);
    }

    @Override
    public void jobsResumed(String group) {
        log.info(getName() + " - the group job is resumed. group:{}", group);
    }

    /**
     * 在 Scheduler 的正常运行期间产生一个严重错误时调用这个方法。错误的类型会各式的，但是下面列举了一些错误例子：
     *     ·初始化 Job 类的问题
     *     ·试图去找到下一 Trigger 的问题
     *     ·JobStore 中重复的问题
     *     ·数据存储连接的问题
     * 你可以使用 SchedulerException 的 getErrorCode() 或者 getUnderlyingException() 方法或获取到特定错误的更详尽的信息。
     */
    @Override
    public void schedulerError(String msg, SchedulerException e) {
        log.error(getName() + " - the scheduler has error. errorMsg:{} ,errorCode:{}", msg, e);
    }

    @Override
    public void schedulerInStandbyMode() {
        log.info(getName() + ".schedulerInStandbyMode()");
    }

    @Override
    public void schedulerShutdown() {
        log.info(getName() + " - the scheduler shutdown is called.");
    }

    @Override
    public void schedulerShuttingdown() {
        log.info(getName() + " - the scheduler is shutting down.");
    }

    @Override
    public void schedulerStarted() {
        log.info(getName() + " - the scheduler is started.");
    }

    @Override
    public void schedulerStarting() {
        log.info(getName() + " - the scheduler is starting.");
    }

    @Override
    public void schedulingDataCleared() {
        log.info(getName() + " - the scheduler data is cleared.");
    }

    /**
     * 当一个 Trigger 来到了再也不会触发的状态时调用这个方法。
     *
     * @param trigger
     */
    @Override
    public void triggerFinalized(Trigger trigger) {
        String triggerName = trigger.getKey().getName();
        String jobName = trigger.getJobKey().getName();
        log.info(getName() + " - the job:{} is trigger finalized. triggerName:{}", jobName, triggerName);
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        String triggerName = triggerKey.getName();
        log.info(getName() + " - the trigger is paused. triggerName:{}", triggerName);
    }


    @Override
    public void triggersPaused(String group) {
        log.info(getName() + " - the trigger group is paused. groupName:{}", group);
    }

    @Override
    public void triggersResumed(String group) {
        log.info(getName() + " - the trigger group is resumed. groupName:{}", group);
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        log.info(getName() + " - the trigger is resumed. triggerKey:{}", triggerKey);
    }

}
