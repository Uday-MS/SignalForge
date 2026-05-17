package com.uptimerobot.dto;

import org.springframework.stereotype.Component;


public class pingResult {
    private long responseTime;
    private String status;

    public pingResult(long responseTime, String status) {
        this.responseTime = responseTime;
        this.status = status;
    }

    public pingResult() {
    }

    public long getResponseTime() {
        return responseTime;
    }

    public String getStatus() {
        return status;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
