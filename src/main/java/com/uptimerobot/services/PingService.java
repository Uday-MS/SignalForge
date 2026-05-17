package com.uptimerobot.services;
import com.uptimerobot.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.module.Configuration;

@Service
public class PingService {

    private RestTemplate restTemplate;
    @Autowired
    public PingService(RestTemplate restTemplate){
        this.restTemplate=restTemplate;
    }

    public pingResult checkStatus(String url){
            try{
                long start= System.currentTimeMillis();
                ResponseEntity<String>response= restTemplate.getForEntity(url,String.class);
                long end= System.currentTimeMillis();
                HttpStatusCode statusCode = response.getStatusCode();
                if(statusCode.is2xxSuccessful()){
                    return new pingResult(end-start,"UP");
                }
                if(statusCode.is3xxRedirection())return new pingResult(end-start, "REDIRECTED");
                if(statusCode.is4xxClientError())return new pingResult(end-start, "CLIENT_ERROR");
                if(statusCode.is5xxServerError())return new pingResult(end-start, "SERVER_ERROR");
            } catch (Exception e){

            }
            try{
                Thread.sleep(500);
            }catch (InterruptedException ignored){};
        return new pingResult(-1,"UNKNOWN_ERROR");
    }
}
