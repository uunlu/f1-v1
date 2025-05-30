package com.f1.seasonchampions.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClient;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {
  @Bean
  @Primary
  public TaskScheduler taskScheduler() {
    final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(2);
    taskScheduler.setThreadNamePrefix("f1-race-winner-task");
    taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
    taskScheduler.setAwaitTerminationSeconds(60);
    return taskScheduler;
  }

  @Bean
  public RestClient restClient() {
    return RestClient.builder()
        .defaultHeaders(
            httpHeaders -> {
              httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
              httpHeaders.add("User-Agent", "F1-Season-Champions/1.0");
            })
        .build();
  }
}
