package com.uptimerobot.services;

import com.uptimerobot.dto.StatsResponse;
import com.uptimerobot.dto.pingResult;
import com.uptimerobot.entity.MonitoredUrl;
import com.uptimerobot.repository.JPARepo;
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

    @Autowired private JPARepo repo;
    @Autowired private PingService pingService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private AtomicReference<StatsResponse> cachedStats =
            new AtomicReference<>(new StatsResponse(0,0,0));

    public void save(MonitoredUrl url) {
        repo.save(url);
    }

    public void deletebyId(int id) {
        if (!repo.existsById(id)) throw new RuntimeException("URL does not exist");
        repo.deleteById(id);
    }

    public Optional<MonitoredUrl> findById(int id) {
        return repo.findById(id);
    }

    public void ping() {
        List<MonitoredUrl> urlList = repo.findAll();
        long Up=urlList.stream().filter(u->"UP".equals(u.getLastStatus())).count();
        cachedStats.set(new StatsResponse(urlList.size(),Up,urlList.size()-Up));
        for (MonitoredUrl url : urlList) {
            executorService.submit(() -> {
                // FIX: pingResult is now a local variable, not shared state
                pingResult result = pingService.checkStatus(url.getUrl());
                url.setLast_status(result.getStatus());
                url.setResponseTime(result.getResponseTime());
                url.setLastchecked(LocalDateTime.now());
                repo.save(url);
            });
        }
    }
    public StatsResponse getCachedStats(){
        return cachedStats.get();
    }
}
