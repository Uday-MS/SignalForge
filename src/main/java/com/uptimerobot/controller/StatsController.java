package com.uptimerobot.controller;

import com.uptimerobot.services.MonitoredUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uptimerobot")
public class StatsController {
    @Autowired
    private MonitoredUrlService monitoredUrlService;

    @GetMapping("/stats")
    public ResponseEntity<?>getStats(){
        return ResponseEntity.ok().body(monitoredUrlService.getCachedStats());
    }
}
