package com.signalforge.services;

import com.signalforge.dto.PingResult;
import com.signalforge.dto.StatsResponse;
import com.signalforge.entity.MonitoredUrl;
import com.signalforge.repository.MonitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MonitoredUrlService {

    @Autowired
    private MonitorRepository repo;
    @Autowired
    private PingService pingService;
    @Autowired
    private AlertService alertService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final AtomicReference<StatsResponse> cachedStats =
            new AtomicReference<>(new StatsResponse(0, 0, 0));

    public void save(MonitoredUrl url) {
        repo.save(url);
    }

    public void deleteById(int id) {
        if (!repo.existsById(id)) throw new RuntimeException("URL does not exist");
        repo.deleteById(id);
    }

    public Optional<MonitoredUrl> findById(int id) {
        return repo.findById(id);
    }

    public void ping() {
        List<MonitoredUrl> urlList = repo.findAll();
        long up = urlList.stream().filter(u -> "UP".equals(u.getLastStatus())).count();
        cachedStats.set(new StatsResponse(urlList.size(), up, urlList.size() - up));

        for (MonitoredUrl url : urlList) {
            executorService.submit(() -> {
                String previousStatus = url.getLastStatus();
                PingResult result = pingService.checkStatus(url.getUrl());
                String newStatus = result.getStatus();

                url.setLastStatus(newStatus);
                url.setResponseTime(result.getResponseTime());
                url.setLastChecked(LocalDateTime.now());
                repo.save(url);

                // Check for status changes and send alerts
                if (previousStatus != null && !previousStatus.equals(newStatus)) {
                    alertService.handleStatusChange(url, previousStatus, newStatus);
                }
            });
        }
    }

    public StatsResponse getCachedStats() {
        return cachedStats.get();
    }
}
