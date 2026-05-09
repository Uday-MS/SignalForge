package com.uptimerobot.configuration;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.io.Closeable;
import java.time.Duration;

@Configuration
public class Appconfig {
    @Bean
    public ObjectMapper objectMapper(){
        return  new ObjectMapper();
    }

    @Bean
    //configured restTemplate
    public RestTemplate configuredRestTemplate(){
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setResponseTimeout(10000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
        CloseableHttpClient client= HttpClients.custom().setDefaultRequestConfig(config).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
        factory.setReadTimeout(3000);
    return new RestTemplate(factory);
    }
}
