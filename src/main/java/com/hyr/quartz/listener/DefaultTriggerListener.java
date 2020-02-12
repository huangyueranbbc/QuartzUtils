package com.hyr.quartz.listener;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * @date 2018-11-11 下午 11:11
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description: TriggerListener 触发器监听器
 ******************************************************************************/
public class DefaultTriggerListener implements TriggerListener {

    private static Logger log = LoggerFactory.getLogger(DefaultTriggerListener.class);

    private String name; // 监听器名称

    @Override
    public String getName() {
        return this.name;
    }

    public DefaultTriggerListener(String name) {
        this.name = name;
    }

    /**
     * 1
     * Trigger被激发 它关联的job即将被运行
     *
     * @param trigger
     * @param jobExecutionContext
     */
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
        String triggerName = trigger.getKey().getName();
        String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info(getName() + " - the triggerName:{} is trigger. jobName:{}", triggerName, jobName);
    }

    /**
     * 2
     * Trigger被激发 它关联的job即将被运行,先执行(1)，在执行(2) 如果返回TRUE 那么任务job会被终止
     *
     * @param trigger
     * @param jobExecutionContext
     * @return
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
        return false;
    }

    /**
     * 3
     * 当Trigger错过被激发时执行,比如当前时间有很多触发器都需要执行，但是线程池中的有效线程都在工作，
     * 那么有的触发器就有可能超时，错过这一轮的触发。
     *
     * @param trigger
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
        String triggerName = trigger.getKey().getName();
        String jobName = trigger.getJobKey().getName();
        log.warn(getName() + " - the job is misfire. triggerName:{} ,jobName:{}", triggerName, jobName);
    }

    /**
     * 4
     * 任务完成时触发
     *
     * @param trigger
     * @param jobExecutionContext
     * @param completedExecutionInstruction
     */
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, Trigger.CompletedExecutionInstruction completedExecutionInstruction) {
        String triggerName = trigger.getKey().getName();
        String jobName = trigger.getJobKey().getName();
        log.info(getName() + " - the trigger is trigger complete. jobName:{} ,triggerName:{}", jobName, triggerName);
    }
}
