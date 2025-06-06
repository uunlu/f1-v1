package com.f1.seasonchampions.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Centralized service for making rate-limited API calls to external F1 APIs. Implements retry logic
 * with exponential backoff for 429 (Too Many Requests) errors.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitedApiClientService {

  private final RateLimiter f1ApiRateLimiter;
  private final RestTemplate restTemplate;

  /**
   * Makes a rate-limited GET request with retry logic for 429 errors.
   *
   * @param url The URL to make the request to
   * @param responseType The expected response type
   * @param <T> The response type
   * @return ResponseEntity containing the response
   * @throws RestClientException if the request fails after all retries
   */
  @Retryable(
      retryFor = {HttpClientErrorException.TooManyRequests.class, RestClientException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000))
  public <T> ResponseEntity<T> executeRateLimitedRequest(
      final String url, final Class<T> responseType) {

    log.debug("Making rate-limited request to: {}", url);

    final Supplier<ResponseEntity<T>> rateLimitedCall =
        RateLimiter.decorateSupplier(
            this.f1ApiRateLimiter,
            () -> {
              try {
                final ResponseEntity<T> response =
                    this.restTemplate.getForEntity(url, responseType);
                log.debug("Successfully received response from: {}", url);
                return response;
              } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn("Rate limit exceeded for URL: {}, will retry with backoff", url);
                throw e;
              } catch (RestClientException e) {
                log.error("API request failed for URL: {} - {}", url, e.getMessage());
                throw e;
              }
            });

    try {
      return rateLimitedCall.get();
    } catch (Exception e) {
      log.error(
          "Rate-limited request failed after rate limiting for URL: {} - {}", url, e.getMessage());
      throw e;
    }
  }

  /**
   * Makes a rate-limited request with a custom supplier function.
   *
   * @param requestSupplier The supplier function that makes the actual API call
   * @param description Description of the operation for logging
   * @param <T> The return type
   * @return The result of the API call
   */
  public <T> T executeRateLimitedOperation(
      final Supplier<T> requestSupplier, final String description) {

    log.debug("Executing rate-limited operation: {}", description);

    final Supplier<T> rateLimitedCall =
        RateLimiter.decorateSupplier(this.f1ApiRateLimiter, requestSupplier);

    try {
      final T result = rateLimitedCall.get();
      log.debug("Successfully completed rate-limited operation: {}", description);
      return result;
    } catch (Exception e) {
      log.error("Rate-limited operation failed: {} - {}", description, e.getMessage());
      throw e;
    }
  }
}
