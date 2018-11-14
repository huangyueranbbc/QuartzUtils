package com.hyr.quartz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/*******************************************************************************
 * Description: 业务计算入口
 ******************************************************************************/
public enum JobService {
    instances;

    private static Logger log = LoggerFactory.getLogger(JobService.class);

    public void update() {
        System.out.println("the job is run......");
        int runTime = new Random().nextInt(50);
        log.info("runTime:{}", runTime);
        for (int i = 1; i <= runTime; i++) {
            try {
                log.info("the job is run.... times:{}", i);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
