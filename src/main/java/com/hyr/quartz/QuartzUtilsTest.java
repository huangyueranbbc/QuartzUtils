package com.hyr.quartz;

import com.hyr.quartz.common.Constant;
import com.hyr.quartz.common.JOB_STORE_CLASS;
import com.hyr.quartz.job.MyJob;
import com.hyr.quartz.job.QuartzJob;
import com.hyr.quartz.listener.DefaultJobListener;
import com.hyr.quartz.listener.DefaultSchedulerListener;
import com.hyr.quartz.listener.DefaultTriggerListener;
import com.hyr.quartz.plugin.QuartzLoggingJobHistoryPlugin;
import com.hyr.quartz.plugin.QuartzLoggingTriggerHistoryPlugin;
import com.hyr.quartz.plugin.QuartzShutdownHookPlugin;
import com.hyr.quartz.service.JobService;
import com.hyr.quartz.utils.HookPriority;
import com.hyr.quartz.utils.MxBeanManager;
import com.hyr.quartz.utils.QuartzUtils;
import com.hyr.quartz.utils.ShutdownHookManager;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*******************************************************************************
 * @date 2018-11-06 下午 2:20
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description: 测试类
 *
 * 私有全局变量放到{@link Constant}
 * Job持久化类型{@link JOB_STORE_CLASS}
 * Quartz工具类{@link QuartzUtils}
 * Quartz任务日志插件{@link QuartzLoggingJobHistoryPlugin}
 * Quartz触发器日志插件{@link QuartzLoggingTriggerHistoryPlugin}
 * Quartz服务优雅关闭插件{@link QuartzShutdownHookPlugin}
 * Quartz监听器{@link com.hyr.quartz.listener}
 * ShutHook的优先级{@link HookPriority}
 * JVM监控服务{@link MxBeanManager}
 * 带有优先级的关闭钩子工具类{@link ShutdownHookManager}
 * Quartz任务的抽象类,抽象封装一些通用方法{@link QuartzJob}
 *
 * @see MyJob 定时任务类,封装任务的执行逻辑
 * @see JobService 定时任务业务类,封装了定时任务具体的业务逻辑,枚举单例模式
 * @see QuartzTest Quartz原生调用方式测试类
 *
 ******************************************************************************/
public class QuartzUtilsTest {

    private static final String GROUP_NAME = "QUARTZ-JOB-GROUP";


    public static void main(String[] args) throws SchedulerException {
        StdSchedulerFactory schedulerFactory1 = QuartzUtils.getStdSchedulerFactory(2, Thread.NORM_PRIORITY, "UPLOAD_JOB1", JOB_STORE_CLASS.JOBSTORETX);
        Scheduler scheduler = schedulerFactory1.getScheduler();

        StdSchedulerFactory schedulerFactory2 = QuartzUtils.getStdSchedulerFactory(2, Thread.NORM_PRIORITY, "UPLOAD_JOB2", JOB_STORE_CLASS.JOBSTORETX);
        Scheduler scheduler2 = schedulerFactory2.getScheduler();

        //QuartzUtils.addSchedulerShutdownHook(scheduler);
        //QuartzUtils.addSchedulerShutdownHook(scheduler2);

        QuartzUtils.startLogPlugin(scheduler, QuartzUtils.LOG_INFO); // 启动日志插件
        QuartzUtils.startShutDownHookPlugin(scheduler); // 启动ShutDownHook插件


        QuartzUtils.startLogPlugin(scheduler2, QuartzUtils.LOG_DEBUG); // 启动日志插件
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

        // 执行定时任务
        QuartzUtils.scheduleWithFixedDelay(scheduler, MyJob.class, 0, 1, TimeUnit.SECONDS, -1, "ProducerJob", GROUP_NAME);

        // 删除任务,如果任务被删除，该持久化信息也会清除,该任务无法恢复
        // QuartzUtils.removeJob(scheduler,"ProducerJob", GROUP_NAME);

        QuartzUtils.start(scheduler);

        // 注入属性
        //QuartzUtils.scheduleWithFixedDelay(scheduler2, MyJob.class, 0, 2, TimeUnit.SECONDS, -1, "ProducerJobData1", GROUP_NAME, dataMap);

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("任务已执行10秒.");
//        System.out.println("开始暂停所有任务.");
//        QuartzUtils.pasueAllJob(scheduler);
//        System.out.println("所有任务暂停成功!");
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("任务已暂停10秒.");
//        System.out.println("开始恢复所有任务.");
//        QuartzUtils.resumeJob(scheduler,"ProducerJob",GROUP_NAME);
//        //QuartzUtils.resumeAllJob(scheduler);
//        System.out.println("所有任务恢复成功!");
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("任务已执行10秒.");
//        System.out.println("开始移除任务.");
//        QuartzUtils.removeJob(scheduler,"ProducerJob", GROUP_NAME);
//        System.out.println("移除任务成功!");


        //System.exit(0);
    }

}
