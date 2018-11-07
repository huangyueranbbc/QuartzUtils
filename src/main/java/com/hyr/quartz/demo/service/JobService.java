package com.hyr.quartz.demo.service;

/*******************************************************************************
 * Description: 业务计算入口
 ******************************************************************************/
public enum JobService {
    instances;

    public void update() {
        System.out.println("the job is run......");
    }

}
