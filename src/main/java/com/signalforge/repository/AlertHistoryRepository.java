package com.signalforge.repository;

import com.signalforge.entity.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    List<AlertHistory> findTop20ByOrderByCreatedAtDesc();
    List<AlertHistory> findByMonitorUserIdOrderByCreatedAtDesc(Integer userId);
}
