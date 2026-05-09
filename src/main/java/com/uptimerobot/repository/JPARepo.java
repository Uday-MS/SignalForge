package com.uptimerobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uptimerobot.entity.MonitoredUrl;
public interface JPARepo extends JpaRepository<MonitoredUrl,Integer> {

}
