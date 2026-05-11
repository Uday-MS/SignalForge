package com.uptimerobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uptimerobot.entity.MonitoredUrl;

import java.util.List;

public interface JPARepo extends JpaRepository<MonitoredUrl,Integer> {
    List<MonitoredUrl> findByUserId(Integer userId);
}
