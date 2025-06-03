package com.f1.seasonchampions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SeasonChampionsApplication {
  public static void main(final String[] args) {
    SpringApplication.run(SeasonChampionsApplication.class, args);
  }
}
