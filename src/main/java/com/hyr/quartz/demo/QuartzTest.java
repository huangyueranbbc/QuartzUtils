package com.hyr.quartz.demo;

import com.hyr.quartz.demo.listener.MyJobListener;
import com.hyr.quartz.demo.listener.MySchedulerListener;
import com.hyr.quartz.demo.listener.MyTriggerListener;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * This is a RAM Store Quartz!
 */
public class QuartzTest {

    private static Logger _log = Logger.getLogger(QuartzTest.class);

    public static void main(String[] args) throws SchedulerException {
        //1.创建Scheduler的工厂
        SchedulerFactory sf = new StdSchedulerFactory();
        //2.从工厂中获取调度器实例
        Scheduler scheduler = sf.getScheduler();


        ListenerManager listenerManager = scheduler.getListenerManager();
        listenerManager.addSchedulerListener(new MySchedulerListener());
        listenerManager.addJobListener(new MyJobListener()); // 任务监听
        listenerManager.addTriggerListener(new MyTriggerListener()); // 触发器监听

        //3.创建JobDetail
        JobDetail jb = JobBuilder.newJob(MyJob.class)
                .storeDurably(true) // 如果一个job是非持久的，当没有活跃的trigger与之关联的时候，会被自动地从scheduler中删除
                .requestRecovery(true) // job可恢复。scheduler发生硬关闭（hard shutdown)（比如运行的进程崩溃了，或者关机了），则当scheduler重新启动的时候，该job会被重新执行。
                .withDescription("this is a ram job") //job的描述
                .withIdentity("ramJob", "ramGroup") //job 的name和group
                .build();

        //任务运行的时间，SimpleSchedle类型触发器有效
        long time = System.currentTimeMillis() + 3 * 1000L; //3秒后启动任务
        Date statTime = new Date(time);

        //4.创建Trigger
        //使用SimpleScheduleBuilder或者CronScheduleBuilder
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0/2 * * * * ?");

        // 设置misfire错失补偿机制

        // 以当前时间为触发频率立刻触发一次执行,然后按照Cron频率依次执行.会合并部分的misfire,正常执行下一个周期的任务.
        // 假设9，10的任务都misfire了，系统在10：15分起来了。只会执行一次misfire，下次正点执行。
        cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed(); // 默认
        // cronScheduleBuilder.withMisfireHandlingInstructionDoNothing(); // 所有的misfire不管，执行下一个周期的任务)
        // cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires(); //所有misfire的任务会马上执行

        Trigger trigger = TriggerBuilder.newTrigger()
                .withDescription("")
                .withPriority(Trigger.DEFAULT_PRIORITY) // 优先级
                .withIdentity("ramTrigger", "ramTriggerGroup")
                //.withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .startAt(statTime)  //默认当前时间启动
                .withSchedule(cronScheduleBuilder) //两秒执行一次
                .build();

        //5.注册任务和定时器
        scheduler.scheduleJob(jb, trigger);

        //6.启动 调度器
        scheduler.start();
        _log.info("启动时间 ： " + new Date());

        try {
            Thread.sleep(6000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // scheduler.deleteJob(jb.getKey()); // 删除job

    }
}