package com.hyr.quartz.utils;

import com.hyr.quartz.common.Constant;
import com.hyr.quartz.common.JOB_STORE_CLASS;
import com.hyr.quartz.job.QuartzJob;
import com.hyr.quartz.plugin.QuartzLoggingJobHistoryPlugin;
import com.hyr.quartz.plugin.QuartzLoggingTriggerHistoryPlugin;
import com.hyr.quartz.plugin.QuartzShutdownHookPlugin;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.SimpleClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*******************************************************************************
 * @date 2018-11-13 下午 5:53
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description: Quartz工具类
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
 ******************************************************************************/
public class QuartzUtils {

    private final static Logger log = LoggerFactory.getLogger(QuartzUtils.class);

    private static ShutdownHookManager shutdownHookManager = ShutdownHookManager.get();

    // quartz配置文件
    private volatile static Properties properties = null;

    // QuartzLoggingPlugin 日志级别

    @SuppressWarnings("WeakerAccess")
    public final static int LOG_TRACE = 0;

    @SuppressWarnings("WeakerAccess")
    public final static int LOG_DEBUG = 10;

    @SuppressWarnings("WeakerAccess")
    public final static int LOG_INFO = 20;

    @SuppressWarnings("WeakerAccess")
    public final static int LOG_WARN = 30;

    @SuppressWarnings("WeakerAccess")
    public final static int LOG_ERROR = 40;

    /**
     * @param threadCount      线程数
     * @param threadPriority   线程优先级 5默认优先级
     * @param threadNamePrefix 工作线程池中线程名称的前缀将被附加前缀
     * @return
     * @throws SchedulerException
     */
    public static StdSchedulerFactory getStdSchedulerFactory(int threadCount, int threadPriority, String threadNamePrefix, JOB_STORE_CLASS job_store_class) throws SchedulerException {
        Properties props = getProperties();
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", String.valueOf(threadCount)); // 线程数
        props.setProperty("org.quartz.threadPool.threadPriority", String.valueOf(threadPriority)); // 线程优先级 5默认优先级
        props.setProperty("org.quartz.threadPool.threadNamePrefix", threadNamePrefix); // 工作线程池中线程名称的前缀将被附加前缀
        props.setProperty("org.quartz.jobStore.class", job_store_class.getClassName()); // 将job数据保存在ram,性能最高。但程序崩溃，job调度数据会丢失。
        props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        return new StdSchedulerFactory(props);
    }

    public static StdSchedulerFactory getStdSchedulerFactory(Properties props) throws SchedulerException {
        return new StdSchedulerFactory(props);
    }

    public static StdSchedulerFactory getStdSchedulerFactory(String schedulerName, JOB_STORE_CLASS job_store_class) throws SchedulerException {
        Properties props = getProperties();
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "1"); // 线程数
        props.setProperty("org.quartz.threadPool.threadPriority", String.valueOf(Thread.NORM_PRIORITY)); // 线程优先级 5默认优先级
        props.setProperty("org.quartz.threadPool.threadNamePrefix", schedulerName); // 工作线程池中线程名称的前缀将被附加前缀
        props.setProperty("org.quartz.jobStore.class", job_store_class.getClassName()); // 将job数据保存在ram,性能最高。但程序崩溃，job调度数据会丢失。
        props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        return new StdSchedulerFactory(props);
    }

    /**
     * 加载配置文件
     *
     * @return
     */
    public static Properties getProperties() {
        if (properties == null) {
            synchronized (Properties.class) {
                if (properties == null) {
                    Properties props = new Properties();
                    InputStream inputStream = null;
                    try {
                        // 获取进程内环境变量中quartz配置路径
                        String configPath = System.getProperty(Constant.QUARTZ_CONF_ENV_NAME, "");
                        File file = new File(configPath);
                        if (file.isFile() && configPath.length() != 0) {
                            log.info("load quartz external configuration. path:{}", file.getPath());
                            // 文件存在
                            inputStream = new BufferedInputStream(new FileInputStream(file));
                        } else {
                            // 文件不存在,在家项目中的默认配置
                            log.info("load quartz default configuration.");
                            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("quartz.properties");
                        }

                        if (inputStream != null) {
                            props.load(inputStream);
                        }
                        properties = props;
                    } catch (Exception e) {
                        log.error("get quartz properties error.", e);
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            log.error("input stream close error.",e);
                        }
                    }
                }
            }
        }
        return properties;
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
     * 绑定多个监听器
     *
     * @param scheduler
     * @param schedulerListeners
     * @param jobListeners
     * @param triggerListeners
     * @throws SchedulerException
     */
    public static void bindSchedulerListenerManagers(Scheduler scheduler, Collection<SchedulerListener> schedulerListeners, Collection<JobListener> jobListeners, Collection<TriggerListener> triggerListeners) throws SchedulerException {
        ListenerManager listenerManager = scheduler.getListenerManager();
        if (schedulerListeners != null && !schedulerListeners.isEmpty()) {
            for (SchedulerListener schedulerListener : schedulerListeners) {
                listenerManager.addSchedulerListener(schedulerListener);
            }
        }
        if (jobListeners != null && !jobListeners.isEmpty()) {
            for (JobListener jobListener : jobListeners) {
                listenerManager.addJobListener(jobListener);
            }
        }
        if (triggerListeners != null && !triggerListeners.isEmpty()) {
            for (TriggerListener triggerListener : triggerListeners) {
                listenerManager.addTriggerListener(triggerListener);
            }
        }
    }

    /**
     * 延时启动
     *
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     时间单位
     * @param timer        cron表达式
     * @param jobName      任务名称
     * @param groupName    组名
     */
    public static void scheduleWithFixedDelayByCron(Scheduler scheduler, Class<? extends Job> job, long initialDelay, TimeUnit timeUnit, String timer, String jobName, String groupName) throws SchedulerException {
        try {
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetail(job, jobName, groupName);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getTrigger(timer, jobName, groupName, statTime);

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("job:{} is start. timer:{}", jobName, timer);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, timer:{}", jobName, timer, e);
            throw e;
        }
    }

    /**
     * 延时启动
     *
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     时间单位
     * @param timer        cron表达式
     * @param jobName      任务名称
     * @param groupName    组名
     * @param dataMap      属性注入 以JavaBean的形式注入，需要该属性名和set方法
     */
    public static void scheduleWithFixedDelayByCron(Scheduler scheduler, Class<? extends Job> job, long initialDelay, TimeUnit timeUnit, String timer, String jobName, String groupName, JobDataMap dataMap) throws SchedulerException {
        try {
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetailBindData(job, jobName, groupName, dataMap);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getTrigger(timer, jobName, groupName, statTime);

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("job:{} is start. timer:{}", jobName, timer);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, timer:{}", jobName, timer, e);
            throw e;
        }
    }

    /**
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     时间单位
     * @param delay        间隔时间
     * @param repeatCount  重复执行次数 -1无限次数 0不执行
     * @param jobName      任务名称
     * @param groupName    组名
     */
    public static void scheduleWithFixedDelay(Scheduler scheduler, Class<? extends Job> job, long initialDelay, long delay, TimeUnit timeUnit, int repeatCount, String jobName, String groupName) throws SchedulerException {
        long intervalTime = timeUnit.toMillis(delay);
        try {
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetail(job, jobName, groupName);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getSimpleTrigger(intervalTime, jobName, groupName, repeatCount, statTime);

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("job:{} is start. delay:{}", jobName, delay);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, intervalTime:{}", jobName, intervalTime, e);
            throw e;
        }
    }

    /**
     * @param scheduler    调度器
     * @param job          JobClass
     * @param initialDelay 首次延时启动时间
     * @param timeUnit     时间单位
     * @param delay        间隔时间
     * @param repeatCount  重复执行次数 -1无限次数 0不执行
     * @param jobName      任务名称
     * @param groupName    组名
     * @param dataMap      属性注入 以JavaBean的形式注入，需要该属性名和set方法
     */
    public static void scheduleWithFixedDelay(Scheduler scheduler, Class<? extends Job> job, long initialDelay, long delay, TimeUnit timeUnit, int repeatCount, String jobName, String groupName, JobDataMap dataMap) throws SchedulerException {
        long intervalTime = timeUnit.toMillis(delay);
        try {
            long delayMillis = timeUnit.toMillis(initialDelay); // 延时启动时间
            JobDetail jobDetail = getJobDetailBindData(job, jobName, groupName, dataMap);
            long tmpTime = System.currentTimeMillis() + delayMillis; //延迟启动任务时间
            Date statTime = new Date(tmpTime); // 启动时间

            Trigger trigger = getSimpleTrigger(intervalTime, jobName, groupName, repeatCount, statTime);

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("job:{} is start. delay:{}", jobName, delay);
        } catch (Exception e) {
            log.error("add job error. jobName:{}, intervalTime:{}", jobName, intervalTime, e);
            throw e;
        }
    }

    private static Trigger getSimpleTrigger(long delay, String jobName, String groupName, int repeatCount, Date statTime) {
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder.withIntervalInMilliseconds(delay).withRepeatCount(repeatCount);
        simpleScheduleBuilder.withMisfireHandlingInstructionFireNow(); // 以当前时间为触发频率立即触发执行

        return TriggerBuilder.newTrigger()
                .withPriority(Trigger.DEFAULT_PRIORITY) // 优先级
                .withIdentity(String.format(Constant.TRIGGER_NAME_FORMAT, jobName), String.format(Constant.TRIGGER_GROUP_NAME_FORMAT, groupName))
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
                .withIdentity(String.format(Constant.TRIGGER_NAME_FORMAT, jobName), String.format(Constant.TRIGGER_GROUP_NAME_FORMAT, groupName))
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
                .withIdentity(String.format(Constant.TRIGGER_NAME_FORMAT, jobName), String.format(Constant.TRIGGER_GROUP_NAME_FORMAT, groupName))
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
                .withIdentity(String.format(Constant.TRIGGER_NAME_FORMAT, jobName), String.format(Constant.TRIGGER_GROUP_NAME_FORMAT, groupName))
                .startAt(statTime)  //默认当前时间启动
                .withSchedule(cronScheduleBuilder) //两秒执行一次
                .usingJobData(dataMap) // 注入属性
                .build();
    }

    private static JobDetail getJobDetail(Class<? extends Job> job, String jobName, String groupName) {
        return JobBuilder.newJob(job)
                .withIdentity(jobName, groupName) //job 的name和group
                .requestRecovery(true) // job可恢复。scheduler发生硬关闭（hard shutdown)（比如运行的进程崩溃了，或者关机了），则当scheduler重新启动的时候，该job会被重新执行。
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
        TriggerKey triggerKey = TriggerKey.triggerKey(String.format(Constant.TRIGGER_NAME_FORMAT, jobName), String.format(Constant.TRIGGER_GROUP_NAME_FORMAT, groupName));
        scheduler.pauseTrigger(triggerKey);// 停止触发器
        scheduler.unscheduleJob(triggerKey);// 移除触发器
        // 获取调度器中所有的触发器组
        List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
        List<String> triggerNames = scheduler.getTriggerGroupNames();
        // 重新恢复指定的触发器任务
        for (String triggerGroupName : triggerGroupNames) {
            for (String triggerName : triggerNames) {
                Trigger tg = scheduler.getTrigger(new TriggerKey(triggerName, triggerGroupName));
                // 如存在该任务,进行恢复运行
                if (tg instanceof SimpleTrigger
                        && tg.getDescription().equals(groupName + "." + jobName)) {
                    boolean result = scheduler.deleteJob(JobKey.jobKey(jobName, groupName));
                    log.info("delete job result:{}", result);
                }
            }
        }
    }

    /**
     * 恢复所有任务
     *
     * @param scheduler
     */
    public static void resumeAllJob(Scheduler scheduler) throws SchedulerException {
        // 恢复所有时，该方法传入监听器中触发器的值为null,监听器可能会报控制针
        scheduler.resumeAll();
    }

    /**
     * 从数据库中找到指定的job，重新开始执行该定时任务
     */
    public static void resumeJob(Scheduler scheduler, String jobName, String groupName) throws SchedulerException {
        scheduler.resumeJob(new JobKey(jobName, groupName));
    }

    /**
     * 暂停所有任务
     *
     * @param scheduler
     * @throws SchedulerException
     */
    public static void pasueAllJob(Scheduler scheduler) throws SchedulerException {
        scheduler.pauseAll();
    }

    /**
     * 暂停指定任务
     *
     * @param scheduler
     * @param jobName
     * @param groupName
     * @throws SchedulerException
     */
    public static void pasueJob(Scheduler scheduler, String jobName, String groupName) throws SchedulerException {
        // 获取调度器中所有的触发器组
        List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
        List<String> triggerNames = scheduler.getTriggerGroupNames();
        // 重新恢复指定的触发器任务
        for (String triggerGroupName : triggerGroupNames) {
            for (String triggerName : triggerNames) {
                Trigger tg = scheduler.getTrigger(new TriggerKey(triggerName, triggerGroupName));
                // 如存在该任务,进行恢复运行
                if (tg instanceof SimpleTrigger && tg.getDescription().equals(groupName + "." + jobName)) {
                    scheduler.pauseJob(new JobKey(triggerName, triggerGroupName));
                }
            }
        }
    }

    /**
     * 获取当前所有运行的任务
     */
    public static List<JobExecutionContext> listExecutingJobs(Scheduler scheduler) throws SchedulerException {
        return scheduler.getCurrentlyExecutingJobs();
    }

    /**
     * 获取运行中指定的任务
     */
    public static JobExecutionContext getExecutingJobsByJobName(Scheduler scheduler, String jobName) throws SchedulerException {
        List<JobExecutionContext> jobContexts = scheduler.getCurrentlyExecutingJobs();
        for (JobExecutionContext context : jobContexts) {
            // 该任务名存在,表示任务正在执行
            if (jobName.equals(context.getTrigger().getJobKey().getName())) {
                return context;
            }
        }
        return null;
    }

    /**
     * 获取系统中已添加的所有任务
     *
     * @param scheduler
     * @return
     */
    public static Set<JobKey> listJobs(Scheduler scheduler) throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.<JobKey>anyGroup());
    }

    /**
     * 获取系统中已添加的指定的任务
     *
     * @param scheduler
     * @return
     */
    public static JobKey getJobByGroupAndName(Scheduler scheduler, String jobGroupName, String jobName) throws SchedulerException {
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.<JobKey>anyGroup());
        for (JobKey jobKey : jobKeys) {
            String key = jobKey.getGroup() + "." + jobKey.getName();
            if (key.equals(jobGroupName + "." + jobName)) {
                return jobKey;
            }
        }
        return null;
    }


    /**
     * 启动命令 会启动调度访问并恢复上次持久化的任务,如任务已被删除则无法恢复
     *
     * @param scheduler
     * @throws SchedulerException
     */
    public static void start(Scheduler scheduler) throws SchedulerException {
        scheduler.start();
        scheduler.resumeAll();
    }

    /**
     * 启动日志插件
     *
     * @param scheduler
     * @param log_level 日志统一打印级别
     */
    public static void startLogPlugin(Scheduler scheduler, int log_level) {
        try {
            String schedulerName = scheduler.getSchedulerName();
            // trigger log plugin
            QuartzLoggingTriggerHistoryPlugin triggerLogPlugin = new QuartzLoggingTriggerHistoryPlugin();
            triggerLogPlugin.initialize(schedulerName, scheduler, new SimpleClassLoadHelper());
            triggerLogPlugin.setLog_level(log_level);
            // job log plugin
            QuartzLoggingJobHistoryPlugin jobLogPlugin = new QuartzLoggingJobHistoryPlugin();
            jobLogPlugin.initialize(schedulerName, scheduler, new SimpleClassLoadHelper());
            jobLogPlugin.setLog_level(log_level);

            addPluginShutdownHook(triggerLogPlugin);
            addPluginShutdownHook(jobLogPlugin);

            triggerLogPlugin.start();
            jobLogPlugin.start();
        } catch (SchedulerException e) {
            log.error("start log plugin error.", e);
        }
    }

    /**
     * 启动ShutDownHook插件
     *
     * @param scheduler
     */
    public static void startShutDownHookPlugin(Scheduler scheduler) {
        try {
            String schedulerName = scheduler.getSchedulerName();
            QuartzShutdownHookPlugin shutdownHookPlugin = new QuartzShutdownHookPlugin();
            shutdownHookPlugin.initialize(schedulerName, scheduler, new SimpleClassLoadHelper());
            addPluginShutdownHook(shutdownHookPlugin);
            shutdownHookPlugin.start();
        } catch (Exception e) {
            log.error("start shutdown hook plugin error.", e);
        }
    }

    /**
     * plugin shutdownhook
     *
     * @param schedulerPlugin
     */
    public static void addPluginShutdownHook(final SchedulerPlugin schedulerPlugin) {
        Thread schedulerShutdownHook = new Thread() {
            @Override
            public void run() {
                if (schedulerPlugin != null) {
                    synchronized (schedulerPlugin) {
                        schedulerPlugin.shutdown();
                        log.info("scheduler plugin:{} shutdown success.", schedulerPlugin.getClass().getSimpleName());
                    }
                }
            }
        };
        shutdownHookManager.addShutdownHook(schedulerShutdownHook, HookPriority.PLUGIN_PRIORITY.value());
    }

    /**
     * Job ShutdownHook
     *
     * @param scheduler
     * @param jobName
     * @param groupName
     */
//    @Deprecated
//    public static void addJobShutdownHook(final Scheduler scheduler, final String jobName, final String groupName) {
//        Thread quartzJobShutdownHook = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    if (scheduler != null) {
//                        synchronized (scheduler) {
//                            if (!scheduler.isShutdown()) {
//                                System.out.println("开始移除所有任务");
//                                removeJob(scheduler, jobName, groupName);
//                            }
//                            log.info("job:{} remove success.", jobName);
//                        }
//                    }
//                } catch (Exception e) {
//                    log.error("delete job error. jobName:{}, groupName:{}", jobName, groupName, e);
//                }
//            }
//        };
//        // 注:该优先级要比Schedule的Hook优先级高
//        shutdownHookManager.addShutdownHook(quartzJobShutdownHook, HookPriority.JOB_PRIORITY.value());
//    }

    /**
     * Scheduler ShutdownHook
     * 避免和Quartz自带ShutdownPlugin插件同时使用
     *
     * @param scheduler
     */
    public static void addSchedulerShutdownHook(final Scheduler scheduler) {
        Thread schedulerShutdownHook = new Thread() {
            @Override
            public void run() {
                try {
                    if (scheduler != null) {
                        synchronized (scheduler) {
                            String schedulerName = scheduler.getSchedulerName();
                            if (!scheduler.isShutdown()) {
                                scheduler.shutdown(true);
                            }
                            log.info("scheduler shutdown success. scheduler:{}", schedulerName);
                        }
                    }
                } catch (Exception e) {
                    log.error("shutdown scheduler error.", e);
                }
            }
        };
        // 注:该优先级要比Job的Hook优先级低
        shutdownHookManager.addShutdownHook(schedulerShutdownHook, HookPriority.SCHEDULER_PRIORITY.value());
    }

}
