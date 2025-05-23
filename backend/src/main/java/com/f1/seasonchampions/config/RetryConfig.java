package com.f1.seasonchampions.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRetry // This enables Spring Retry functionality
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure the backoff policy - determines how long to wait between retries
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 second initial delay
        backOffPolicy.setMultiplier(2.0);       // Double the wait time for each retry
        backOffPolicy.setMaxInterval(10000);    // Maximum 10 seconds delay
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Configure which exceptions should trigger a retry
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(RestClientException.class, true);

        // Configure retry policy - how many times to retry and on which exceptions
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
