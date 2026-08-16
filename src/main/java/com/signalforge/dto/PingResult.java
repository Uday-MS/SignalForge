package com.signalforge.dto;

public class PingResult {
    private long responseTime;
    private String status;

    public PingResult() {}

    public PingResult(long responseTime, String status) {
        this.responseTime = responseTime;
        this.status = status;
    }

    public long getResponseTime() { return responseTime; }
    public String getStatus() { return status; }

    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
    public void setStatus(String status) { this.status = status; }
}
