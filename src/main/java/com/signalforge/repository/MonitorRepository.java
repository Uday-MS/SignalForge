package com.signalforge.repository;

import com.signalforge.entity.MonitoredUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorRepository extends JpaRepository<MonitoredUrl, Integer> {
    List<MonitoredUrl> findByUserId(Integer userId);
    long countByUserIdAndLastStatus(Integer userId, String lastStatus);
}
