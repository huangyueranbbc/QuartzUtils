package com.hyr.quartz.demo.listener;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/*******************************************************************************
 * @date 2018-10-19 下午 4:13
 * @author: <a href=mailto:huangyr@bonree.com>黄跃然</a>
 * @Description: TriggerListener 触发器监听器
 ******************************************************************************/
public class MyTriggerListener implements TriggerListener {

    private static int execCount = 0;

    @Override
    public String getName() {
        return "MyTriggerListener";
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
        System.out.println("Trigger监听器：MyTriggerListener.triggerFired()");
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
        System.out.println("Trigger监听器：MyTriggerListener.vetoJobExecution()");
        if (execCount == 10) { // job执行10次，终止
            return true;
        }
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
        System.out.println("Trigger监听器：MyTriggerListener.triggerMisfired()");
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
        execCount++;
        System.out.println("Trigger监听器：MyTriggerListener.triggerComplete() count:" + execCount);
    }
}
