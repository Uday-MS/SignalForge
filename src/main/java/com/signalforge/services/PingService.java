package com.signalforge.services;

import com.signalforge.dto.PingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PingService {

    private final RestTemplate restTemplate;

    @Autowired
    public PingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PingResult checkStatus(String url) {
        try {
            long start = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            long end = System.currentTimeMillis();
            HttpStatusCode statusCode = response.getStatusCode();

            if (statusCode.is2xxSuccessful()) return new PingResult(end - start, "UP");
            if (statusCode.is3xxRedirection()) return new PingResult(end - start, "REDIRECTED");
            if (statusCode.is4xxClientError()) return new PingResult(end - start, "CLIENT_ERROR");
            if (statusCode.is5xxServerError()) return new PingResult(end - start, "SERVER_ERROR");
        } catch (Exception e) {
            // Connection failed — site is DOWN
        }
        return new PingResult(-1, "DOWN");
    }
}
