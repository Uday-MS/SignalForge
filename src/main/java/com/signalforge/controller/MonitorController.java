package com.signalforge.controller;

import com.signalforge.entity.MonitoredUrl;
import com.signalforge.entity.User;
import com.signalforge.services.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @PostMapping("/monitors")
    public ResponseEntity<?> addMonitor(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody MonitoredUrl url) {
        User user = monitorService.getUser(userDetails);
        monitorService.addUrl(user.getId(), url);
        return ResponseEntity.ok(Map.of("message", "Monitor added successfully"));
    }

    @GetMapping("/monitors")
    public ResponseEntity<List<MonitoredUrl>> getMonitors(@AuthenticationPrincipal UserDetails userDetails) {
        User user = monitorService.getUser(userDetails);
        return ResponseEntity.ok(monitorService.getUrls(user.getId()));
    }

    @DeleteMapping("/monitors/{monitorId}")
    public ResponseEntity<?> deleteMonitor(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer monitorId) {
        monitorService.deleteUrl(monitorId, userDetails);
        return ResponseEntity.ok(Map.of("message", "Monitor deleted successfully"));
    }
}
