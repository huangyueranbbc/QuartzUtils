package com.hyr.quartz.common;

/*******************************************************************************
 *
 * @date 2020-02-04 9:57 AM
 * @author: <a href=mailto:@huangyr>黄跃然</a>
 * @Description: Job持久化参数
 ******************************************************************************/
public enum JOB_STORE_CLASS {

    //  正常启动
    RAMJOBSTORE("org.quartz.simpl.RAMJobStore"),
    // 更新
    JOBSTORETX("org.quartz.impl.jdbcjobstore.JobStoreTX");

    private String className = null;

    JOB_STORE_CLASS(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
