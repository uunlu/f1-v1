package com.f1.seasonchampions.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized rate limiter configuration for F1 API calls. Implements the requirements from
 * RateLimit.md: 1 request per 10 seconds with 30 second timeout.
 */
@Configuration
@Slf4j
public class ApiRateLimiterConfig {

  private static final int LIMIT_FOR_PERIOD = 1;
  private static final int LIMIT_REFRESH_PERIOD_SECONDS = 3;
  private static final int TIMEOUT_DURATION_SECONDS = 30;

  @Bean
  public RateLimiterConfig rateLimiterConfig() {
    return RateLimiterConfig.custom()
        .limitForPeriod(LIMIT_FOR_PERIOD)
        .limitRefreshPeriod(Duration.ofSeconds(LIMIT_REFRESH_PERIOD_SECONDS))
        .timeoutDuration(Duration.ofSeconds(TIMEOUT_DURATION_SECONDS))
        .build();
  }

  @Bean
  public RateLimiterRegistry rateLimiterRegistry(final RateLimiterConfig rateLimiterConfig) {
    return RateLimiterRegistry.of(rateLimiterConfig);
  }

  @Bean
  public RateLimiter f1ApiRateLimiter(final RateLimiterRegistry rateLimiterRegistry) {
    final RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("f1-api-rate-limiter");
    log.info(
        "Initialized F1 API rate limiter: {} requests per {} seconds",
        LIMIT_FOR_PERIOD,
        LIMIT_REFRESH_PERIOD_SECONDS);
    return rateLimiter;
  }
}
