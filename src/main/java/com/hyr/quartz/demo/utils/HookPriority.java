package com.hyr.quartz.demo.utils;

/**
 * shutdown hook priority
 */
public enum HookPriority {

    MIN_PRIORITY(1),
    SCHEDULER_PRIORITY(20),
    PLUGIN_PRIORITY(40),
    NORM_PRIORITY(50),
    JOB_PRIORITY(60),
    MAX_PRIORITY(100);


    private int priority;

    HookPriority(int priority) {
        this.priority = priority;
    }

    public int value() {
        return priority;
    }
}
