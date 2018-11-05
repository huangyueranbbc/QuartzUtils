package com.hyr.quartz.demo;

/*******************************************************************************
 * Description: 业务计算入口
 ******************************************************************************/
public enum JobBusiness {
    instances;

    public void update() {
        System.out.println("the job is run......");
    }

}
