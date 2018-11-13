package com.hyr.quartz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * Description: 业务计算入口
 ******************************************************************************/
public enum JobService {
    instances;

    private static Logger log = LoggerFactory.getLogger(JobService.class);

    public void update() {
        System.out.println("the job is run......");
        for (int i = 1; i <= 10; i++) {
            try {
                log.info("the job is run.... times:{}", i);
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
