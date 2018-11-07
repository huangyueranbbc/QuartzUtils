package com.hyr.quartz.demo.utils;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/*******************************************************************************
 * @date 2018-11-11 下午 11:11
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description: Quartz工具类
 ******************************************************************************/
public class QuartzUtils {

    private static Logger log = LoggerFactory.getLogger(QuartzUtils.class);

    private static ShutdownHookManager shutdownHookManager = ShutdownHookManager.get(); // shutdownhook

    /**
     * @param threadCount      线程数
     * @param threadPriority   线程优先级 5默认优先级
     * @param threadNamePrefix 工作线程池中线程名称的前缀将被附加前缀
     * @param schedulerName    实例名称
     * @return
     * @throws SchedulerException
     */
    public static StdSchedulerFactory getStdSchedulerFactory(int threadCount, int threadPriority, String threadNamePrefix, String schedulerName) throws SchedulerException {
        Properties props = new Properties();
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", String.valueOf(threadCount)); // 线程数
        props.setProperty("org.quartz.threadPool.threadPriority", String.valueOf(threadPriority)); // 线程优先级 5默认优先级
        props.setProperty("org.quartz.threadPool.threadNamePrefix", threadNamePrefix); // 工作线程池中线程名称的前缀将被附加前缀
        props.setProperty("org.quartz.scheduler.instanceName", schedulerName); // 实例名称
        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory(props);
        addSchedulerFactoryHook(stdSchedulerFactory);
        return stdSchedulerFactory;
    }

    public static StdSchedulerFactory getStdSchedulerFactory(Properties props) throws SchedulerException {
        return new StdSchedulerFactory(props);
    }

    public static StdSchedulerFactory getStdSchedulerFactory(String schedulerName) throws SchedulerException {
        Properties props = new Properties();
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "4"); // 线程数
        props.setProperty("org.quartz.threadPool.threadPriority", String.valueOf(Thread.NORM_PRIORITY)); // 线程优先级 5默认优先级
        props.setProperty("org.quartz.threadPool.threadNamePrefix", schedulerName); // 工作线程池中线程名称的前缀将被附加前缀
        props.setProperty("org.quartz.scheduler.instanceName", schedulerName); // 实例名称
        return new StdSchedulerFactory(props);
    }

    /**
     * 绑定监听器
     *
     * @param scheduler
     * @param schedulerListener
     * @param jobListener
     * @param triggerListener
     * @throws SchedulerException
     */
    public static void bindSchedulerListenerManager(Scheduler scheduler, SchedulerListener schedulerListener, JobListener jobListener, TriggerListener triggerListener) throws SchedulerException {
        ListenerManager listenerManager = scheduler.getListenerManager();
        if (schedulerListener != null) {
            listenerManager.addSchedulerListener(schedulerListener);
        }
        if (jobListener != null) {
            listenerManager.addJobListener(jobListener);
        }
        if (triggerListener != null) {
            listenerManager.addTriggerListener(triggerListener);
        }
    }

    /**
     * 延时启动
     *
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     延时启动时间单位
     * @param timer        cron表达式
     * @param jobName      任务名称
     * @param groupName    组名
     */
    public static void scheduleWithFixedDelayByCron(Scheduler scheduler, Class<? extends Job> job, long initialDelay, TimeUnit timeUnit, String timer, String jobName, String groupName) {
        try {
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetail(job, jobName, groupName);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getTrigger(timer, jobName, groupName, statTime);

            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
            log.info("job:{} is start. timer:{}", jobName, timer);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, timer:{}", jobName, timer, e);
        }
    }

    /**
     * 延时启动
     *
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     延时启动时间单位
     * @param timer        cron表达式
     * @param jobName      任务名称
     * @param groupName    组名
     * @param dataMap      属性注入 以JavaBean的形式注入，需要该属性名和set方法
     */
    public static void scheduleWithFixedDelayByCron(Scheduler scheduler, Class<? extends Job> job, long initialDelay, TimeUnit timeUnit, String timer, String jobName, String groupName, JobDataMap dataMap) {
        try {
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetailBindData(job, jobName, groupName, dataMap);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getTrigger(timer, jobName, groupName, statTime);

            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
            log.info("job:{} is start. timer:{}", jobName, timer);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, timer:{}", jobName, timer, e);
        }
    }

    /**
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     延时启动时间单位
     * @param delay        间隔时间
     * @param repeatCount  重复执行次数 -1无限次数 0不执行
     * @param jobName      任务名称
     * @param groupName    组名
     */
    public static void scheduleWithFixedDelay(Scheduler scheduler, Class<? extends Job> job, long initialDelay, long delay, TimeUnit timeUnit, int repeatCount, String jobName, String groupName) {
        long intervalTime = timeUnit.toMillis(delay);
        try {
            addJobHook(scheduler, jobName, groupName);
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetail(job, jobName, groupName);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getSimpleTrigger(intervalTime, jobName, groupName, repeatCount, statTime);

            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
            log.info("job:{} is start. delay:{}", jobName, delay);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, intervalTime:{}", jobName, intervalTime, e);
        }
    }

    /**
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     延时启动时间单位
     * @param delay        间隔时间
     * @param repeatCount  重复执行次数 -1无限次数 0不执行
     * @param jobName      任务名称
     * @param groupName    组名
     * @param dataMap      属性注入 以JavaBean的形式注入，需要该属性名和set方法
     */
    public static void scheduleWithFixedDelay(Scheduler scheduler, Class<? extends Job> job, long initialDelay, long delay, TimeUnit timeUnit, int repeatCount, String jobName, String groupName, JobDataMap dataMap) {
        long intervalTime = timeUnit.toMillis(delay);
        try {
            addJobHook(scheduler, jobName, groupName);
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetailBindData(job, jobName, groupName, dataMap);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getSimpleTrigger(intervalTime, jobName, groupName, repeatCount, statTime);

            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
            log.info("job:{} is start. delay:{}", jobName, delay);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, intervalTime:{}", jobName, intervalTime, e);
        }
    }

    private static Trigger getSimpleTrigger(long delay, String jobName, String groupName, int repeatCount, Date statTime) {
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder.withIntervalInMilliseconds(delay).withRepeatCount(repeatCount);
        simpleScheduleBuilder.withMisfireHandlingInstructionFireNow(); // 以当前时间为触发频率立即触发执行

        return TriggerBuilder.newTrigger()
                .withPriority(Trigger.DEFAULT_PRIORITY) // 优先级
                .withIdentity("Trigger-" + jobName, "Trigger-" + groupName)
                .startAt(statTime)  //默认当前时间启动
                .withSchedule(simpleScheduleBuilder) //两秒执行一次
                .build();
    }

    /**
     * 给trigger下所有job注入属性
     *
     * @param delay       间隔时间
     * @param repeatCount 重复执行次数 -1无限次数 0不执行
     * @param jobName     任务名称
     * @param groupName   组名
     * @param statTime    开始执行时间
     * @param dataMap     属性注入 以JavaBean的形式注入，需要该属性名和set方法
     * @return
     */
    private static Trigger getSimpleTriggerBindData(long delay, String jobName, String groupName, int repeatCount, Date statTime, JobDataMap dataMap) {
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder.withIntervalInMilliseconds(delay).withRepeatCount(repeatCount);
        simpleScheduleBuilder.withMisfireHandlingInstructionFireNow(); // 以当前时间为触发频率立即触发执行

        return TriggerBuilder.newTrigger()
                .withPriority(Trigger.DEFAULT_PRIORITY) // 优先级
                .withIdentity("Trigger-" + jobName, "Trigger-" + groupName)
                .startAt(statTime)  //默认当前时间启动
                .withSchedule(simpleScheduleBuilder) //两秒执行一次
                .usingJobData(dataMap) // 输入属性
                .build();
    }

    private static Trigger getTrigger(String timer, String jobName, String groupName, Date statTime) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(timer);
        cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed(); // 默认 以当前时间为触发频率立刻触发一次执行,然后按照Cron频率依次执行.会合并部分的misfire,正常执行下一个周期的任务.
        // cronScheduleBuilder.withMisfireHandlingInstructionDoNothing(); // 所有的misfire不管，执行下一个周期的任务)
        // cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires(); //所有misfire的任务会马上执行

        return TriggerBuilder.newTrigger()
                .withPriority(Trigger.DEFAULT_PRIORITY) // 优先级
                .withIdentity("Trigger-" + jobName, "Trigger-" + groupName)
                .startAt(statTime)  //默认当前时间启动
                .withSchedule(cronScheduleBuilder) //两秒执行一次
                .build();
    }

    /**
     * 给trigger下所有的Job注入属性
     *
     * @param timer     cron表达式
     * @param jobName   任务名称
     * @param groupName 组名
     * @param statTime  开始执行时间
     * @param dataMap   属性注入 以JavaBean的形式注入，需要该属性名和set方法
     * @return
     */
    private static Trigger getTriggerBindData(String timer, String jobName, String groupName, Date statTime, JobDataMap dataMap) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(timer);
        cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed(); // 默认 以当前时间为触发频率立刻触发一次执行,然后按照Cron频率依次执行.会合并部分的misfire,正常执行下一个周期的任务.
        // cronScheduleBuilder.withMisfireHandlingInstructionDoNothing(); // 所有的misfire不管，执行下一个周期的任务)
        // cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires(); //所有misfire的任务会马上执行

        return TriggerBuilder.newTrigger()
                .withPriority(Trigger.DEFAULT_PRIORITY) // 优先级
                .withIdentity("Trigger-" + jobName, "Trigger-" + groupName)
                .startAt(statTime)  //默认当前时间启动
                .withSchedule(cronScheduleBuilder) //两秒执行一次
                .usingJobData(dataMap) // 注入属性
                .build();
    }

    private static JobDetail getJobDetail(Class<? extends Job> job, String jobName, String groupName) {
        return JobBuilder.newJob(job)
                .storeDurably(true) // 如果一个job是非持久的，当没有活跃的trigger与之关联的时候，会被自动地从scheduler中删除
                .requestRecovery(true) // job可恢复。scheduler发生硬关闭（hard shutdown)（比如运行的进程崩溃了，或者关机了），则当scheduler重新启动的时候，该job会被重新执行。
                .withIdentity(jobName, groupName) //job 的name和group
                //.usingJobData("jobDesc", "job_01") // 属性注入
                .build();
    }

    /**
     * @param job       任务对象
     * @param jobName   任务名称
     * @param groupName 组名
     * @param dataMap   属性注入 以JavaBean的形式注入，需要该属性名和set方法
     * @return
     */
    private static JobDetail getJobDetailBindData(Class<? extends Job> job, String jobName, String groupName, JobDataMap dataMap) {
        return JobBuilder.newJob(job)
                .storeDurably(true) // 如果一个job是非持久的，当没有活跃的trigger与之关联的时候，会被自动地从scheduler中删除
                .requestRecovery(true) // job可恢复。scheduler发生硬关闭（hard shutdown)（比如运行的进程崩溃了，或者关机了），则当scheduler重新启动的时候，该job会被重新执行。
                .withIdentity(jobName, groupName) //job 的name和group
                .usingJobData(dataMap) // 属性注入
                .build();
    }

    /**
     * 删除定时Job
     *
     * @param scheduler
     * @param jobName
     * @param groupName
     * @throws SchedulerException
     */
    public static void removeJob(Scheduler scheduler, String jobName, String groupName) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(jobName, groupName));
    }


    /**
     * Job ShutdownHook
     *
     * @param scheduler
     * @param jobName
     * @param groupName
     */
    private static void addJobHook(final Scheduler scheduler, final String jobName, final String groupName) {
        Thread quartzJobShutdownHook = new Thread() {
            @Override
            public void run() {
                try {
                    removeJob(scheduler, jobName, groupName);
                    log.info("job shutdown.");
                } catch (SchedulerException e) {
                    log.error("delete job error. jobName:{}, groupName:{}", jobName, groupName);
                }
            }
        };
        // 注该处的优先级要比Schedule的Hook优先级高
        shutdownHookManager.addShutdownHook(quartzJobShutdownHook, HookPriority.NORM_PRIORITY.value());
    }

    /**
     * Scheduler ShutdownHook
     *
     * @param stdSchedulerFactory
     */
    private static void addSchedulerFactoryHook(final StdSchedulerFactory stdSchedulerFactory) {
        Thread schedulerShutdownHook = new Thread() {
            @Override
            public void run() {
                try {
                    for (Scheduler scheduler : stdSchedulerFactory.getAllSchedulers()) {
                        scheduler.shutdown(true); // true:等待job执行完毕
                    }
                    log.info("scheduler shutdown.");
                } catch (SchedulerException e) {
                    log.error("shutdown scheduler error.");
                }
            }
        };

        // 注该处的优先级要比Job的Hook优先级低
        shutdownHookManager.addShutdownHook(schedulerShutdownHook, HookPriority.MIN_PRIORITY.value());
    }
}
