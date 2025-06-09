package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
@EnableRetry
class RateLimitedApiClientServiceTest {

  @Mock private RestTemplate restTemplate;

  private RateLimitedApiClientService rateLimitedApiClientService;
  private RateLimiter rateLimiter;

  private String testUrl;
  private ResponseEntity<String> successResponse;

  @BeforeEach
  void setUp() {
    // Create a real RateLimiter for testing
    RateLimiterConfig config =
        RateLimiterConfig.custom()
            .limitForPeriod(10)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(100))
            .build();

    rateLimiter = RateLimiter.of("test-rate-limiter", config);

    rateLimitedApiClientService = new RateLimitedApiClientService(rateLimiter, restTemplate);

    testUrl = "https://ergast.com/api/f1/2023/results.json";
    successResponse = new ResponseEntity<>("Test response body", HttpStatus.OK);
  }

  @Test
  void whenExecuteRateLimitedRequest_thenReturnSuccessfulResponse() {
    // Arrange
    when(restTemplate.getForEntity(testUrl, String.class)).thenReturn(successResponse);

    // Act
    ResponseEntity<String> result =
        rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class);

    // Assert
    assertNotNull(result);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("Test response body", result.getBody());
    verify(restTemplate).getForEntity(testUrl, String.class);
  }

  @Test
  void whenRestTemplateThrowsRestClientException_thenPropagateException() {
    // Arrange
    RestClientException exception = new RestClientException("Network error");
    when(restTemplate.getForEntity(testUrl, String.class)).thenThrow(exception);

    // Act & Assert
    RestClientException thrown =
        assertThrows(
            RestClientException.class,
            () -> rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class));

    assertEquals("Network error", thrown.getMessage());
    verify(restTemplate).getForEntity(testUrl, String.class);
  }

  @Test
  void whenResourceAccessException_thenPropagateException() {
    // Arrange
    ResourceAccessException exception = new ResourceAccessException("Connection timeout");
    when(restTemplate.getForEntity(testUrl, String.class)).thenThrow(exception);

    // Act & Assert
    ResourceAccessException thrown =
        assertThrows(
            ResourceAccessException.class,
            () -> rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class));

    assertEquals("Connection timeout", thrown.getMessage());
    verify(restTemplate).getForEntity(testUrl, String.class);
  }

  @Test
  void whenTooManyRequestsException_thenPropagateException() {
    // Arrange
    HttpClientErrorException exception =
        HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too many requests", null, null, null);
    when(restTemplate.getForEntity(testUrl, String.class)).thenThrow(exception);

    // Act & Assert
    HttpClientErrorException thrown =
        assertThrows(
            HttpClientErrorException.class,
            () -> rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class));

    assertEquals(HttpStatus.TOO_MANY_REQUESTS, thrown.getStatusCode());
    verify(restTemplate).getForEntity(testUrl, String.class);
  }

  @Test
  void whenHttpClientErrorException_thenPropagateException() {
    // Arrange
    HttpClientErrorException exception =
        HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null);
    when(restTemplate.getForEntity(testUrl, String.class)).thenThrow(exception);

    // Act & Assert
    HttpClientErrorException thrown =
        assertThrows(
            HttpClientErrorException.class,
            () -> rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class));

    assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
    verify(restTemplate).getForEntity(testUrl, String.class);
  }

  @Test
  void whenDifferentResponseType_thenReturnCorrectType() {
    // Arrange
    class TestResponse {
      private String data;

      public TestResponse(String data) {
        this.data = data;
      }

      public String getData() {
        return data;
      }
    }

    TestResponse testResponseBody = new TestResponse("test data");
    ResponseEntity<TestResponse> testResponse =
        new ResponseEntity<>(testResponseBody, HttpStatus.OK);

    when(restTemplate.getForEntity(testUrl, TestResponse.class)).thenReturn(testResponse);

    // Act
    ResponseEntity<TestResponse> result =
        rateLimitedApiClientService.executeRateLimitedRequest(testUrl, TestResponse.class);

    // Assert
    assertNotNull(result);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals("test data", result.getBody().getData());
    verify(restTemplate).getForEntity(testUrl, TestResponse.class);
  }

  @Test
  void whenExecuteRateLimitedOperationSucceeds_thenReturnResult() {
    // Arrange
    String expectedResult = "Operation successful";
    Supplier<String> testSupplier = () -> expectedResult;
    String description = "Test operation";

    // Act
    String result =
        rateLimitedApiClientService.executeRateLimitedOperation(testSupplier, description);

    // Assert
    assertEquals(expectedResult, result);
  }

  @Test
  void whenExecuteRateLimitedOperationThrowsException_thenPropagateException() {
    // Arrange
    RuntimeException operationException = new RuntimeException("Operation failed");
    Supplier<String> testSupplier =
        () -> {
          throw operationException;
        };
    String description = "Test operation";

    // Act & Assert
    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () ->
                rateLimitedApiClientService.executeRateLimitedOperation(testSupplier, description));

    assertEquals("Operation failed", thrown.getMessage());
  }

  @Test
  void whenExecuteRateLimitedOperationWithNullResult_thenReturnNull() {
    // Arrange
    Supplier<String> testSupplier = () -> null;
    String description = "Test operation returning null";

    // Act
    String result =
        rateLimitedApiClientService.executeRateLimitedOperation(testSupplier, description);

    // Assert
    assertNull(result);
  }

  @Test
  void whenExecuteRateLimitedOperationWithComplexReturnType_thenReturnCorrectType() {
    // Arrange
    class ComplexResult {
      private int value;
      private String message;

      public ComplexResult(int value, String message) {
        this.value = value;
        this.message = message;
      }

      public int getValue() {
        return value;
      }

      public String getMessage() {
        return message;
      }
    }

    ComplexResult expectedResult = new ComplexResult(42, "Complex operation result");
    Supplier<ComplexResult> testSupplier = () -> expectedResult;
    String description = "Complex operation";

    // Act
    ComplexResult result =
        rateLimitedApiClientService.executeRateLimitedOperation(testSupplier, description);

    // Assert
    assertNotNull(result);
    assertEquals(42, result.getValue());
    assertEquals("Complex operation result", result.getMessage());
  }

  @Test
  void whenMultipleSuccessfulRequests_thenAllSucceed() {
    // Arrange
    String url1 = "https://ergast.com/api/f1/2023/1/results.json";
    String url2 = "https://ergast.com/api/f1/2023/2/results.json";
    ResponseEntity<String> response1 = new ResponseEntity<>("Response 1", HttpStatus.OK);
    ResponseEntity<String> response2 = new ResponseEntity<>("Response 2", HttpStatus.OK);

    when(restTemplate.getForEntity(url1, String.class)).thenReturn(response1);
    when(restTemplate.getForEntity(url2, String.class)).thenReturn(response2);

    // Act
    ResponseEntity<String> result1 =
        rateLimitedApiClientService.executeRateLimitedRequest(url1, String.class);
    ResponseEntity<String> result2 =
        rateLimitedApiClientService.executeRateLimitedRequest(url2, String.class);

    // Assert
    assertEquals("Response 1", result1.getBody());
    assertEquals("Response 2", result2.getBody());
    verify(restTemplate).getForEntity(url1, String.class);
    verify(restTemplate).getForEntity(url2, String.class);
  }

  @Test
  void whenNonOkHttpStatus_thenReturnResponse() {
    // Arrange
    ResponseEntity<String> notFoundResponse =
        new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
    when(restTemplate.getForEntity(testUrl, String.class)).thenReturn(notFoundResponse);

    // Act
    ResponseEntity<String> result =
        rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class);

    // Assert
    assertNotNull(result);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    assertEquals("Not found", result.getBody());
    verify(restTemplate).getForEntity(testUrl, String.class);
  }

  @Test
  void whenExecuteRateLimitedOperationWithRestTemplateCall_thenWorkCorrectly() {
    // Arrange
    when(restTemplate.getForEntity(testUrl, String.class)).thenReturn(successResponse);

    Supplier<ResponseEntity<String>> requestSupplier =
        () -> restTemplate.getForEntity(testUrl, String.class);
    String description = "REST template operation";

    // Act
    ResponseEntity<String> result =
        rateLimitedApiClientService.executeRateLimitedOperation(requestSupplier, description);

    // Assert
    assertNotNull(result);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("Test response body", result.getBody());
    verify(restTemplate).getForEntity(testUrl, String.class);
  }

  @Test
  void whenRateLimiterConfiguration_thenServiceUsesCorrectConfiguration() {
    // This test verifies that the service accepts a RateLimiter and uses it
    // The actual rate limiting behavior would be tested in integration tests

    // Arrange & Act
    // The service was constructed with a real RateLimiter in setUp()
    when(restTemplate.getForEntity(testUrl, String.class)).thenReturn(successResponse);

    // Execute multiple requests quickly
    ResponseEntity<String> result1 =
        rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class);
    ResponseEntity<String> result2 =
        rateLimitedApiClientService.executeRateLimitedRequest(testUrl, String.class);

    // Assert
    assertNotNull(result1);
    assertNotNull(result2);
    verify(restTemplate, times(2)).getForEntity(testUrl, String.class);
  }
}
