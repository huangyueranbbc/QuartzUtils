package com.hyr.quartz.listener;

import com.hyr.quartz.utils.MxBeanManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/*******************************************************************************
 * @date 2018-11-11 下午 11:11
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description: JobListener 任务监听器
 ******************************************************************************/
public class DefaultJobListener implements JobListener {

    private static Logger log = LoggerFactory.getLogger(DefaultJobListener.class);

    private String name; // 监听器名称

    private static ConcurrentHashMap<String, Long> jobStatus = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return this.name;
    }

    public DefaultJobListener(String name) {
        this.name = name;
    }

    /**
     * job 将要被执行时调用这个方法。
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        // FIXME 测试执行次数 是否正常。 生产环境删除
        String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        String triggerName = jobExecutionContext.getTrigger().getKey().getName();
        if (!jobStatus.containsKey(jobName)) {
            jobStatus.put(jobName, 0L);
        }
        Long jobCount = jobStatus.get(jobName);
        jobStatus.put(jobName, jobCount + 1); // 执行一次
        log.info(getName() + " - the job:{} is will to exec. count:{} ,triggerName:{}", jobName, jobCount, triggerName);
    }

    /**
     * 即将被执行，但又被 TriggerListener 否决了时调用这个方法。
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.warn(getName() + " - the job:{} is vetoed.", jobName);
    }


    /**
     * job 被执行之后调用这个方法。
     *
     * @param jobExecutionContext
     * @param e
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info(getName() + " - the job:{} is exec success.", jobName);
        MxBeanManager.setLog_level(MxBeanManager.LOG_INFO);
        MxBeanManager.loggingMemoryHistoryDebugInfo(); // 打印内存使用信息
        MxBeanManager.loggingThreadHistoryDebugInfo();
    }



}
