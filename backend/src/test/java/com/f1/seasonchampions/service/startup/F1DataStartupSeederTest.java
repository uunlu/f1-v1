package com.f1.seasonchampions.service.startup;

import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.service.scheduler.F1DataSchedulerService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@ExtendWith(MockitoExtension.class)
class F1DataStartupSeederTest {

  @Mock private F1DataSchedulerService schedulerService;
  @Mock private F1DataSeedingStrategy seedingStrategy;
  @Mock private ApplicationReadyEvent applicationReadyEvent;

  @InjectMocks private F1DataStartupSeeder f1DataStartupSeeder;

  @BeforeEach
  void setUp() {
    // Reset all mocks before each test
    reset(schedulerService, seedingStrategy);
  }

  @Test
  void whenSeedingNotNeeded_thenSkipSeedingProcess() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(false);

    // Act
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService, never()).syncSeasonChampions();
    verify(schedulerService, never()).syncAllHistoricalRaces();
  }

  @Test
  void whenSeedingNeededAndBothSyncSucceed_thenCompleteSuccessfully() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(true);

    RaceSyncResult successfulSeasonResult =
        new RaceSyncResult(10, List.of("2020", "2021", "2022"), true, "Season champions synced");
    RaceSyncResult successfulRaceResult =
        new RaceSyncResult(
            250, List.of("Monaco GP", "Spanish GP"), true, "Historical races synced");

    when(schedulerService.syncSeasonChampions()).thenReturn(successfulSeasonResult);
    when(schedulerService.syncAllHistoricalRaces()).thenReturn(successfulRaceResult);

    // Act
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService).syncSeasonChampions();
    verify(schedulerService).syncAllHistoricalRaces();
  }

  @Test
  void whenSeedingNeededAndSeasonSyncFails_thenContinueWithRaceSync() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(true);

    RaceSyncResult failedSeasonResult =
        new RaceSyncResult(0, List.of(), false, "Season sync failed");
    RaceSyncResult successfulRaceResult =
        new RaceSyncResult(150, List.of("British GP"), true, "Historical races synced");

    when(schedulerService.syncSeasonChampions()).thenReturn(failedSeasonResult);
    when(schedulerService.syncAllHistoricalRaces()).thenReturn(successfulRaceResult);

    // Act
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService).syncSeasonChampions();
    verify(schedulerService).syncAllHistoricalRaces(); // Should still continue
  }

  @Test
  void whenSeedingNeededAndRaceSyncFails_thenHandleGracefully() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(true);

    RaceSyncResult successfulSeasonResult =
        new RaceSyncResult(10, List.of("2023"), true, "Season champions synced");
    RaceSyncResult failedRaceResult = new RaceSyncResult(0, List.of(), false, "Race sync failed");

    when(schedulerService.syncSeasonChampions()).thenReturn(successfulSeasonResult);
    when(schedulerService.syncAllHistoricalRaces()).thenReturn(failedRaceResult);

    // Act
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService).syncSeasonChampions();
    verify(schedulerService).syncAllHistoricalRaces();
  }

  @Test
  void whenSeasonSyncThrowsException_thenContinueWithRaceSync() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(true);
    when(schedulerService.syncSeasonChampions()).thenThrow(new RuntimeException("API Error"));

    RaceSyncResult successfulRaceResult =
        new RaceSyncResult(
            200, List.of("Australian GP", "Japanese GP"), true, "Historical races synced");
    when(schedulerService.syncAllHistoricalRaces()).thenReturn(successfulRaceResult);

    // Act - should not throw exception
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService).syncSeasonChampions();
    verify(schedulerService).syncAllHistoricalRaces(); // Should still continue
  }

  @Test
  void whenRaceSyncThrowsException_thenHandleGracefully() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(true);

    RaceSyncResult successfulSeasonResult =
        new RaceSyncResult(10, List.of("2024"), true, "Season champions synced");
    when(schedulerService.syncSeasonChampions()).thenReturn(successfulSeasonResult);
    when(schedulerService.syncAllHistoricalRaces())
        .thenThrow(new RuntimeException("Network Error"));

    // Act - should not throw exception
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService).syncSeasonChampions();
    verify(schedulerService).syncAllHistoricalRaces();
  }

  @Test
  void whenBothSyncOperationsThrowExceptions_thenHandleGracefully() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(true);
    when(schedulerService.syncSeasonChampions())
        .thenThrow(new RuntimeException("Season API Error"));
    when(schedulerService.syncAllHistoricalRaces())
        .thenThrow(new RuntimeException("Race API Error"));

    // Act - should not throw exception
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService).syncSeasonChampions();
    verify(schedulerService).syncAllHistoricalRaces();
  }

  @Test
  void whenSeedingStrategyThrowsException_thenHandleGracefully() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded())
        .thenThrow(new RuntimeException("Database connection error"));

    // Act - should not throw exception
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService, never()).syncSeasonChampions();
    verify(schedulerService, never()).syncAllHistoricalRaces();
  }

  @Test
  void whenSeedingNeededWithPartialSuccess_thenLogAppropriately() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(true);

    RaceSyncResult partialSeasonResult =
        new RaceSyncResult(5, List.of("2019", "2020"), true, "Only 5 seasons updated");
    RaceSyncResult partialRaceResult = new RaceSyncResult(0, List.of(), true, "No new races found");

    when(schedulerService.syncSeasonChampions()).thenReturn(partialSeasonResult);
    when(schedulerService.syncAllHistoricalRaces()).thenReturn(partialRaceResult);

    // Act
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    verify(schedulerService).syncSeasonChampions();
    verify(schedulerService).syncAllHistoricalRaces();
  }

  @Test
  void whenApplicationReadyEvent_thenTriggerSeeding() {
    // Arrange
    when(seedingStrategy.isSeedingNeeded()).thenReturn(false);

    // Act
    f1DataStartupSeeder.seedHistoricalRaceData();

    // Assert
    verify(seedingStrategy).isSeedingNeeded();
    // This test verifies that the method can be called directly (as it would be by Spring's
    // EventListener)
  }
}
