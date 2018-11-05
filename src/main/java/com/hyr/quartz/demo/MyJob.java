package com.hyr.quartz.demo;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@DisallowConcurrentExecution // 不允许并发执行多个Job实例。 当前Job执行完毕后，才会执行下一个。一进一出。
@PersistJobDataAfterExecution // 每次执行JOB后，更新Job内容
public class MyJob implements Job {

    //    private static Logger _log = Logger.getLogger(MyJob.class);
    private static Logger _log = LoggerFactory.getLogger(MyJob.class);

    // 属性注入
    private String jobDesc;

    /**
     * execute方法中仅允许抛出一种类型的异常（包括RuntimeExceptions），即JobExecutionException。你的job可以使用该异常告诉scheduler，你希望如何来处理发生的异常。
     * 如果job发错错误,quartz提供两种方式解决
     * 1 立即重新执行任务
     * 2 立即停止所有相关这个任务的触发器
     *
     * @param jobExecutionContext
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        try {
            _log.info(jobKey + " Say hello to Quartz. " + jobDesc + "  " + new Date());
            long start = System.currentTimeMillis();
            JobBusiness.instances.update();
            long end = System.currentTimeMillis();
            _log.info("UpdateJob cost time is " + (end - start));
            // Thread.sleep(10000); // 睡眠10秒，测试DisallowConcurrentExecution
            // throw new Exception(); // 抛出测试异常
        } catch (Exception e) {
            _log.error(jobKey + " execute has error.", e);
            // 通过JobExecutionException异常,通知scheduler如何处理
            JobExecutionException jee =
                    new JobExecutionException(e);
            // this job will refire immediately
            jee.setRefireImmediately(true); // 立即重新执行任务
            // Quartz will automatically unschedule
            // all triggers associated with this job
            // so that it does not run again
            // jee.setUnscheduleAllTriggers(true); // 立即停止所有相关这个任务的触发器
            throw jee;
        }
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }
}