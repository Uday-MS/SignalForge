package com.uptimerobot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name="MonitoredUrl")
public class MonitoredUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
       private Integer id;

    @Column(name ="url")
    private String url;

    @Column(name="last_status")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String LastStatus;

    @JsonFormat(pattern="dd-MM-yyyy HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name="lastchecked")
    private LocalDateTime lastchecked;

    @Column(name="response_time")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long responseTime;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="user_id")
    private User user;

    public MonitoredUrl(Integer id, String url, String LastStatus, LocalDateTime lastchecked,Long responseTime) {
        this.id = id;
        this.url = url;
        this.LastStatus = LastStatus;
        this.lastchecked = lastchecked;
        this.responseTime=responseTime;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MonitoredUrl() {
    }

    public Integer getId() {

        return id;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getUrl() {

        return url;
    }

    public User getUser() {
        return user;
    }

    public String getLastStatus() {

        return LastStatus;
    }

    public LocalDateTime getLastchecked() {
        return lastchecked;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLast_status(String LastStatus) {
        this.LastStatus = LastStatus;
    }

    public void setLastchecked(LocalDateTime lastchecked) {
        this.lastchecked = lastchecked;
    }
}
