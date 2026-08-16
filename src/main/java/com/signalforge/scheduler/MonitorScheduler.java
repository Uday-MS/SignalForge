package com.signalforge.scheduler;

import com.signalforge.services.MonitoredUrlService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitorScheduler {

    private final MonitoredUrlService monitoredUrlService;

    public MonitorScheduler(MonitoredUrlService monitoredUrlService) {
        this.monitoredUrlService = monitoredUrlService;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    public void runMonitor() {
        monitoredUrlService.ping();
    }
}
