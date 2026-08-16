package com.signalforge.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "monitored_url")
public class MonitoredUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name")
    private String name;

    @Column(name = "last_status")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String lastStatus;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    @Column(name = "response_time")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long responseTime;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    public MonitoredUrl() {}

    public MonitoredUrl(Integer id, String url, String name, String lastStatus, LocalDateTime lastChecked, Long responseTime) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.lastStatus = lastStatus;
        this.lastChecked = lastChecked;
        this.responseTime = responseTime;
    }

    public Integer getId() { return id; }
    public String getUrl() { return url; }
    public String getName() { return name; }
    public String getLastStatus() { return lastStatus; }
    public LocalDateTime getLastChecked() { return lastChecked; }
    public Long getResponseTime() { return responseTime; }
    public User getUser() { return user; }

    public void setId(Integer id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setName(String name) { this.name = name; }
    public void setLastStatus(String lastStatus) { this.lastStatus = lastStatus; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
    public void setUser(User user) { this.user = user; }
}
