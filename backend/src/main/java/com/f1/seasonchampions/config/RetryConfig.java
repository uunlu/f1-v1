package com.f1.seasonchampions.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;

@Configuration
@EnableRetry
public class RetryConfig {

  private static final long INITIAL_INTERVAL = 1000L; // 1 second initial delay
  private static final double MULTIPLIER = 2.0; // Double the wait time for each retry
  private static final long MAX_INTERVAL = 10000L; // Maximum 10 seconds delay
  private static final int MAX_ATTEMPTS = 3; // Number of retry attempts

  @Bean
  public RetryTemplate retryTemplate() {
    final RetryTemplate retryTemplate = new RetryTemplate();

    // Configure the backoff policy - determines how long to wait between retries
    final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(INITIAL_INTERVAL);
    backOffPolicy.setMultiplier(MULTIPLIER);
    backOffPolicy.setMaxInterval(MAX_INTERVAL);
    retryTemplate.setBackOffPolicy(backOffPolicy);

    // Configure which exceptions should trigger a retry
    final Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
    retryableExceptions.put(RestClientException.class, true);

    // Configure retry policy - how many times to retry and on which exceptions
    final SimpleRetryPolicy retryPolicy =
        new SimpleRetryPolicy(MAX_ATTEMPTS, retryableExceptions, true);
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }
}
