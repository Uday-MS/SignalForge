package com.uptimerobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UtptimerobotApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtptimerobotApplication.class, args);
	}

}
