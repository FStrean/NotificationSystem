package ru.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitorService {
    public static void main(String[] args) {
        SpringApplication.run(MonitorService.class);
    }
}