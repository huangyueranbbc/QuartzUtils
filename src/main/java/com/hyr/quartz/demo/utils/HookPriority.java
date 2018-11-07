package com.hyr.quartz.demo.utils;

/**
 * shutdown hook priority
 */
public enum HookPriority {

    MIN_PRIORITY(1),
    NORM_PRIORITY(5),
    MAX_PRIORITY(10);

    private int priority;

    HookPriority(int priority) {
        this.priority = priority;
    }

    public int value() {
        return priority;
    }
}
