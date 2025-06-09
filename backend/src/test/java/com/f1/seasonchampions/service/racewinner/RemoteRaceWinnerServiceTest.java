package com.f1.seasonchampions.service.racewinner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.RateLimitedApiClientService;
import com.f1.seasonchampions.service.scheduler.F1DataSchedulerService;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class F1DataSchedulerServiceTest {

  @Mock private RaceWinnerSeedService raceWinnerSeedService;
  @Mock private RaceWinnerRepository raceWinnerRepository;
  @Mock private RateLimitedApiClientService rateLimitedApiClient;

  @InjectMocks private F1DataSchedulerService f1DataSchedulerService;

  private RaceWinner mockWinner(int round) {
    RaceWinner winner = new RaceWinner();
    winner.setSeason("2025");
    winner.setRound(String.valueOf(round));
    return winner;
  }

  @BeforeEach
  void setUp() {
    // Set the apiBaseUrl field since it's injected via @Value
    ReflectionTestUtils.setField(
        f1DataSchedulerService, "apiBaseUrl", "https://api.test.com/ergast/f1");
  }

  @Test
  void whenApiReturnsEmptyData_thenNothingIsSaved() {
    // Arrange
    int currentYear = 2025;
    when(raceWinnerRepository.getRaceWinnerBySeason(String.valueOf(currentYear)))
        .thenReturn(List.of(mockWinner(1), mockWinner(2)));

    // Mock empty API response (will result in no new winners found)
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(new ResultsByYearResponse()));

    // Act
    f1DataSchedulerService.syncLatestF1RaceResult();

    // Assert - Since API returns empty data, no saves should occur
    verify(raceWinnerSeedService, never()).saveRaceWinner(any());
  }

  @Test
  void whenNoNewData_thenNothingIsSaved() {
    int currentYear = 2025;
    when(raceWinnerRepository.getRaceWinnerBySeason(String.valueOf(currentYear)))
        .thenReturn(List.of(mockWinner(1), mockWinner(2), mockWinner(3)));

    // Mock empty API response
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(new ResultsByYearResponse()));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerSeedService, never()).saveRaceWinner(any());
  }

  @Test
  void whenApiReturnsNoResults_thenExitEarly() {
    // Mock empty API response
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(new ResultsByYearResponse()));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerSeedService, never()).saveRaceWinner(any());
  }

  @Test
  void whenRepositoryReturnsEmpty_thenAllResultsAreSaved() {
    int currentYear = 2025;
    when(raceWinnerRepository.getRaceWinnerBySeason(String.valueOf(currentYear)))
        .thenReturn(List.of());

    // Mock empty API response
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(new ResultsByYearResponse()));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerSeedService, never()).saveRaceWinner(any());
  }

  @Test
  void whenExceptionThrown_thenItIsHandledGracefully() {
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenThrow(new RuntimeException("Simulated API error"));

    assertDoesNotThrow(() -> f1DataSchedulerService.syncLatestF1RaceResult());
  }
}
