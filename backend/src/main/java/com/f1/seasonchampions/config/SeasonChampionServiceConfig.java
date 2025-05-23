package com.f1.seasonchampions.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class SeasonChampionServiceConfig {

  private static final int LIMIT_REFRESH_PERIOD_SECONDS = 1;
  private static final int LIMIT_FOR_PERIOD = 1;
  private static final int TIMEOUT_DURATION_SECONDS = 5;

  @Bean
  public RateLimiter apiRateLimiter() {
    final RateLimiterConfig config = RateLimiterConfig.custom()
      .limitRefreshPeriod(Duration.ofSeconds(LIMIT_REFRESH_PERIOD_SECONDS))
      .limitForPeriod(LIMIT_FOR_PERIOD)
      .timeoutDuration(Duration.ofSeconds(TIMEOUT_DURATION_SECONDS))
      .build();

    final RateLimiterRegistry registry = RateLimiterRegistry.of(config);
    return registry.rateLimiter("apiRateLimiter");
  }
}
