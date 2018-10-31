package com.hyr.quartz.demo.listener;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/*******************************************************************************
 * @date 2018-10-19 下午 4:04
 * @author: <a href=mailto:huangyr@bonree.com>黄跃然</a>
 * @Description: JobListener 任务监听器
 ******************************************************************************/
public class MyJobListener implements JobListener {
    @Override
    public String getName() {
        return "MyJobListener";
    }

    /**
     * job 将要被执行时调用这个方法。
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        System.out.println("准备执行. job:" + jobExecutionContext.getJobDetail().getKey());
    }

    /**
     * 即将被执行，但又被 TriggerListener 否决了时调用这个方法。
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        System.out.println("执行被拒绝. job:" + jobExecutionContext.getJobDetail().getKey());
    }


    /**
     * job 被执行之后调用这个方法。
     *
     * @param jobExecutionContext
     * @param e
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        System.out.println("执行完毕. job:" + jobExecutionContext.getJobDetail().getKey());

    }

}
