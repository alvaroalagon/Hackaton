package com.example.hackaton_01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Hackaton01Application {
    public static void main(String[] args) {
        SpringApplication.run(Hackaton01Application.class, args);
    }
}