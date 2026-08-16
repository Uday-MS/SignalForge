package com.signalforge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_history")
public class AlertHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    private MonitoredUrl monitor;

    @Column(name = "event_type", nullable = false)
    private String eventType; // "DOWN" or "RECOVERED"

    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "response_time")
    private Long responseTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "notified")
    private boolean notified;

    public AlertHistory() {}

    public AlertHistory(MonitoredUrl monitor, String eventType, String statusCode, Long responseTime) {
        this.monitor = monitor;
        this.eventType = eventType;
        this.statusCode = statusCode;
        this.responseTime = responseTime;
        this.createdAt = LocalDateTime.now();
        this.notified = false;
    }

    public Long getId() { return id; }
    public MonitoredUrl getMonitor() { return monitor; }
    public String getEventType() { return eventType; }
    public String getStatusCode() { return statusCode; }
    public Long getResponseTime() { return responseTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isNotified() { return notified; }

    public void setId(Long id) { this.id = id; }
    public void setMonitor(MonitoredUrl monitor) { this.monitor = monitor; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
