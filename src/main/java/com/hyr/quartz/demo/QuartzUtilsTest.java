package com.hyr.quartz.demo;

import com.hyr.quartz.demo.job.MyJob;
import com.hyr.quartz.demo.listener.DefaultJobListener;
import com.hyr.quartz.demo.listener.DefaultSchedulerListener;
import com.hyr.quartz.demo.listener.DefaultTriggerListener;
import com.hyr.quartz.demo.utils.QuartzUtils;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.concurrent.TimeUnit;

/*******************************************************************************
 * @date 2018-11-06 下午 2:20
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description:
 ******************************************************************************/
public class QuartzUtilsTest {

    public static void main(String[] args) throws SchedulerException {
        StdSchedulerFactory schedulerFactory = QuartzUtils.getStdSchedulerFactory(10, Thread.NORM_PRIORITY, "UPLOAD_JOB", "UPLOAD_JOB");
        Scheduler scheduler = schedulerFactory.getScheduler();

        QuartzUtils.bindSchedulerListenerManager(scheduler, new DefaultSchedulerListener("DefaultSchedulerListener"), new DefaultJobListener("DefaultJobListener"), new DefaultTriggerListener("DefaultTriggerListener"));

        JobDataMap dataMap=new JobDataMap();
        dataMap.put("jobDesc","job desc.");

        QuartzUtils.scheduleWithFixedDelay(scheduler, MyJob.class, 0, 5, TimeUnit.SECONDS, -1, "ProducerJob", "QUARTZ-JOB-GROUP");

        // 注入属性
        QuartzUtils.scheduleWithFixedDelay(scheduler, MyJob.class, 0, 10, TimeUnit.SECONDS, -1, "ProducerJobData", "QUARTZ-JOB-GROUP",dataMap);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(-1);
    }

}
