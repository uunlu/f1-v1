package com.f1.seasonchampions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SeasonChampionsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeasonChampionsApplication.class, args);
    }
}