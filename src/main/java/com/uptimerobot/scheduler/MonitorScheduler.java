package com.uptimerobot.scheduler;

import com.uptimerobot.entity.MonitoredUrl;
import com.uptimerobot.services.MonitoredUrlService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitorScheduler {
    private MonitoredUrlService monitoredUrlService;
    public MonitorScheduler(MonitoredUrlService monitoredUrlService){
        this.monitoredUrlService=monitoredUrlService;
    }
    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    public void runMonitor(){
        monitoredUrlService.ping();
    }
}
