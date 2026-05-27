package com.uptimerobot.dto;

public class StatsResponse{
    private long totalUrls;
    private long urlsUp;
    private long urlsDown;

    public StatsResponse(long totalUrls, long urlsUp, long urlsDown) {
        this.totalUrls = totalUrls;
        this.urlsUp = urlsUp;
        this.urlsDown = urlsDown;
    }

    public long getTotalUrls() {
        return totalUrls;
    }

    public void setTotalUrls(long totalUrls) {
        this.totalUrls = totalUrls;
    }

    public long getUrlsUp() {
        return urlsUp;
    }

    public void setUrlsUp(long urlsUp) {
        this.urlsUp = urlsUp;
    }

    public long getUrlsDown() {
        return urlsDown;
    }

    public void setUrlsDown(long urlsDown) {
        this.urlsDown = urlsDown;
    }
}