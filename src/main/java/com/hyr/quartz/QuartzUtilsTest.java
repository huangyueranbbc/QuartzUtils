package com.hyr.quartz;

import com.hyr.quartz.job.MyJob;
import com.hyr.quartz.listener.DefaultJobListener;
import com.hyr.quartz.listener.DefaultSchedulerListener;
import com.hyr.quartz.listener.DefaultTriggerListener;
import com.hyr.quartz.utils.QuartzUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*******************************************************************************
 * @date 2018-11-06 下午 2:20
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description:
 ******************************************************************************/
public class QuartzUtilsTest {

    public static void main(String[] args) throws SchedulerException {
        StdSchedulerFactory schedulerFactory1 = QuartzUtils.getStdSchedulerFactory(10, Thread.NORM_PRIORITY, "UPLOAD_JOB1", "UPLOAD_JOB1");
        Scheduler scheduler = schedulerFactory1.getScheduler();

        StdSchedulerFactory schedulerFactory2 = QuartzUtils.getStdSchedulerFactory(10, Thread.NORM_PRIORITY, "UPLOAD_JOB2", "UPLOAD_JOB2");
        Scheduler scheduler2 = schedulerFactory2.getScheduler();

        QuartzUtils.addSchedulerShutdownHook(scheduler);
        QuartzUtils.addSchedulerShutdownHook(scheduler2);

        QuartzUtils.startLogPlugin(scheduler, QuartzUtils.LOG_ERROR); // 启动日志插件
        QuartzUtils.startShutDownHookPlugin(scheduler); // 启动ShutDownHook插件


        QuartzUtils.startLogPlugin(scheduler2, QuartzUtils.LOG_ERROR); // 启动日志插件
        QuartzUtils.startShutDownHookPlugin(scheduler2); // 启动ShutDownHook插件

        // 绑定单个Listener监听器
        QuartzUtils.bindSchedulerListenerManager(scheduler, new DefaultSchedulerListener("DefaultSchedulerListener"), new DefaultJobListener("DefaultJobListener"), new DefaultTriggerListener("DefaultTriggerListener"));
        QuartzUtils.bindSchedulerListenerManager(scheduler2, new DefaultSchedulerListener("DefaultSchedulerListener"), new DefaultJobListener("DefaultJobListener"), new DefaultTriggerListener("DefaultTriggerListener"));

        // 绑定多个Listener监听器
        List<SchedulerListener> schedulerListeners = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            schedulerListeners.add(new DefaultSchedulerListener("SchedulerListener--" + i));
        }

        List<JobListener> jobListeners = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            jobListeners.add(new DefaultJobListener("JobListener--" + i));
        }

        List<TriggerListener> triggerListeners = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            triggerListeners.add(new DefaultTriggerListener("TriggerListener--" + i));
        }
        // QuartzUtils.bindSchedulerListenerManagers(scheduler, schedulerListeners, jobListeners, triggerListeners);

        // 注入属性Map
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("jobDesc", "job desc.");

        QuartzUtils.scheduleWithFixedDelay(scheduler, MyJob.class, 0, 5, TimeUnit.SECONDS, -1, "ProducerJob", "QUARTZ-JOB-GROUP");

        // 注入属性
        QuartzUtils.scheduleWithFixedDelay(scheduler2, MyJob.class, 0, 10, TimeUnit.SECONDS, -1, "ProducerJobData", "QUARTZ-JOB-GROUP", dataMap);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

}
