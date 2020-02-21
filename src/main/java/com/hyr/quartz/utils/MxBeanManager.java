package com.hyr.quartz.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.*;
import java.text.MessageFormat;

/*******************************************************************************
 * @date 2018-11-14 下午 2:16
 * @author: <a href=mailto:huangyr>黄跃然</a>
 * @Description: MxBeanManager 打印
 ******************************************************************************/
public class MxBeanManager {

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

    private static int log_level = LOG_DEBUG; // 默认只在DEBUG模式打印日志

    private static int getLog_level() {
        return log_level;
    }

    public static void setLog_level(int log_level) {
        MxBeanManager.log_level = log_level;
    }

    private final static Logger log = LoggerFactory.getLogger(MxBeanManager.class);

    private static String heapMemoryMessage = "heapMemory: {0}";
    private static String nonheapMemoryMessage = "nonHeapMemory: {0}";
    private static String threadInfoMessage = "threadInfo: {0}";
    private static String deadLockThreadInfoMessage = "deadlock thread info:{0}";

    /* ============================================================ */
    public static String getHeapMemoryMessage() {
        return heapMemoryMessage;
    }

    public static void setHeapMemoryMessage(String heapMemoryMessage) {
        MxBeanManager.heapMemoryMessage = heapMemoryMessage;
    }

    public static String getNonheapMemoryMessage() {
        return nonheapMemoryMessage;
    }

    public static void setNonheapMemoryMessage(String nonheapMemoryMessage) {
        MxBeanManager.nonheapMemoryMessage = nonheapMemoryMessage;
    }

    public static String getThreadInfoMessage() {
        return threadInfoMessage;
    }

    public static void setThreadInfoMessage(String threadInfoMessage) {
        MxBeanManager.threadInfoMessage = threadInfoMessage;
    }

    public static String getDeadLockThreadInfoMessage() {
        return deadLockThreadInfoMessage;
    }

    public static void setDeadLockThreadInfoMessage(String deadLockThreadInfoMessage) {
        MxBeanManager.deadLockThreadInfoMessage = deadLockThreadInfoMessage;
    }

    // EOF
    /* ============================================================ */

    /**
     * logging memory info
     */
    public static void loggingMemoryHistoryDebugInfo() {
        if (isLogLevelUnabled()) {
            return;
        }

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        logging(MessageFormat.format(getHeapMemoryMessage(), heapMemoryUsage.toString()));
        logging(MessageFormat.format(getNonheapMemoryMessage(), nonHeapMemoryUsage.toString()));
    }

    /**
     * logging thread info. WARN: is method is designed for troubleshooting use, but not for synchronization control. It might be an expensive operation.
     */
    public static void loggingThreadHistoryDebugInfo() {
        if (!log.isTraceEnabled()) {
            return;
        }
        long curThreadId = Thread.currentThread().getId();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        String curThreadInfo = threadMXBean.getThreadInfo(curThreadId).toString();
        log.trace(MessageFormat.format(getThreadInfoMessage(), curThreadInfo));
    }

    /**
     * check deadlock thread info. WARN: is method is designed for troubleshooting use, but not for synchronization control. It might be an expensive operation.
     */
    public static void loggingDeadLockThreadInfo() {
        if (!log.isTraceEnabled()) {
            return;
        }

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
            for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(deadlockedThreadIds)) {
                log.trace(MessageFormat.format(getDeadLockThreadInfoMessage(), threadInfo.toString()));
            }
        }
    }

    /**
     * logging xxxx info  temple
     */
    public static void loggingXXXHistoryDebugInfo() {
        if (isLogLevelUnabled()) {
            return;
        }

    }


    public static Logger getLog() {
        return log;
    }

    /**
     * 判断是否有当前日志级别权限
     *
     * @return
     */
    private static boolean isLogLevelUnabled() {
        switch (getLog_level()) {
            case LOG_TRACE:
                return !getLog().isInfoEnabled();
            case LOG_DEBUG:
                return !getLog().isDebugEnabled();
            case LOG_INFO:
                return !getLog().isInfoEnabled();
            case LOG_WARN:
                return !getLog().isWarnEnabled();
            case LOG_ERROR:
                return !getLog().isErrorEnabled();
        }
        getLog().warn("not fount the log level. level:{}", log_level);
        return true;
    }

    /**
     * 打印日志
     *
     * @param message
     */
    private static void logging(String message) {
        switch (log_level) {
            case LOG_TRACE:
                getLog().trace(message);
                break;
            case LOG_DEBUG:
                getLog().debug(message);
                break;
            case LOG_INFO:
                getLog().info(message);
                break;
            case LOG_WARN:
                getLog().warn(message);
                break;
            case LOG_ERROR:
                getLog().error(message);
                break;
        }
    }

}
