package com.hyr.quartz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/*******************************************************************************
 * Description: 业务计算入口
 ******************************************************************************/
public enum JobService {
    instances;

    private final static Logger log = LoggerFactory.getLogger(JobService.class);

    public void update() {
        log.info("the job is run......");
        int runTime = new Random().nextInt(5);
        log.info("runTime:{}", runTime);
        for (int i = 1; i <= runTime; i++) {
            try {
                log.info("the job is run.... times:{}", i);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("update error.",e);
            }

        }
    }

}
