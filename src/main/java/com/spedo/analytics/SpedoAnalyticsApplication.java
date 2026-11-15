package com.spedo.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpedoAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpedoAnalyticsApplication.class, args);
    }
}
