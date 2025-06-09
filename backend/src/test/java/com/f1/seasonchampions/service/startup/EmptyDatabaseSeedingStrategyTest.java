package com.f1.seasonchampions.service.startup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmptyDatabaseSeedingStrategyTest {

  @Mock private RaceWinnerRepository raceWinnerRepository;
  @Mock private SeasonChampionRepository seasonChampionRepository;

  @InjectMocks private EmptyDatabaseSeedingStrategy seedingStrategy;

  @BeforeEach
  void setUp() {
    reset(raceWinnerRepository, seasonChampionRepository);
  }

  @Test
  void whenBothRepositoriesEmpty_thenSeedingNeeded() {
    // Arrange
    when(seasonChampionRepository.count()).thenReturn(0L);
    when(raceWinnerRepository.count()).thenReturn(0L);

    // Act
    boolean result = seedingStrategy.isSeedingNeeded();

    // Assert
    assertTrue(result, "Should need seeding when both repositories are empty");
    verify(seasonChampionRepository).count();
    verify(raceWinnerRepository).count();
  }

  @Test
  void whenRaceWinnerRepositoryEmptyButSeasonChampionHasData_thenSeedingNeeded() {
    // Arrange
    when(seasonChampionRepository.count()).thenReturn(15L);
    when(raceWinnerRepository.count()).thenReturn(0L);

    // Act
    boolean result = seedingStrategy.isSeedingNeeded();

    // Assert
    assertTrue(result, "Should need seeding when race winner repository is empty");
    verify(seasonChampionRepository).count();
    verify(raceWinnerRepository).count();
  }

  @Test
  void whenSeasonChampionRepositoryEmptyButRaceWinnerHasData_thenSeedingNeeded() {
    // Arrange
    when(seasonChampionRepository.count()).thenReturn(0L);
    when(raceWinnerRepository.count()).thenReturn(250L);

    // Act
    boolean result = seedingStrategy.isSeedingNeeded();

    // Assert
    assertTrue(result, "Should need seeding when season champion repository is empty");
    verify(seasonChampionRepository).count();
    verify(raceWinnerRepository).count();
  }

  @Test
  void whenBothRepositoriesHaveData_thenSeedingNotNeeded() {
    // Arrange
    when(seasonChampionRepository.count()).thenReturn(20L);
    when(raceWinnerRepository.count()).thenReturn(500L);

    // Act
    boolean result = seedingStrategy.isSeedingNeeded();

    // Assert
    assertFalse(result, "Should not need seeding when both repositories have data");
    verify(seasonChampionRepository).count();
    verify(raceWinnerRepository).count();
  }

  @Test
  void whenRepositoriesHaveMinimalData_thenSeedingNotNeeded() {
    // Arrange - testing edge case with minimal data
    when(seasonChampionRepository.count()).thenReturn(1L);
    when(raceWinnerRepository.count()).thenReturn(1L);

    // Act
    boolean result = seedingStrategy.isSeedingNeeded();

    // Assert
    assertFalse(result, "Should not need seeding when both repositories have at least one record");
    verify(seasonChampionRepository).count();
    verify(raceWinnerRepository).count();
  }

  @Test
  void whenSeasonChampionRepositoryThrowsException_thenHandleGracefully() {
    // Arrange
    when(seasonChampionRepository.count()).thenThrow(new RuntimeException("Database timeout"));
    // raceWinnerRepository.count() won't be called because seasonChampionRepository throws first

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> seedingStrategy.isSeedingNeeded(),
        "Should propagate database exceptions to caller for proper error handling");

    verify(seasonChampionRepository).count();
    // Race winner repository should NOT be called since season champion repository throws first
    verify(raceWinnerRepository, never()).count();
  }

  @Test
  void whenRaceWinnerRepositoryThrowsException_thenHandleGracefully() {
    // Arrange
    when(seasonChampionRepository.count()).thenReturn(10L);
    when(raceWinnerRepository.count()).thenThrow(new RuntimeException("Database connection error"));

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> seedingStrategy.isSeedingNeeded(),
        "Should propagate database exceptions to caller for proper error handling");

    verify(seasonChampionRepository).count();
    verify(raceWinnerRepository).count();
  }

  @Test
  void whenBothRepositoriesThrowExceptions_thenHandleGracefully() {
    // Arrange
    when(seasonChampionRepository.count())
        .thenThrow(new RuntimeException("Season champion DB error"));
    // Don't stub raceWinnerRepository since it won't be called due to the first exception

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> seedingStrategy.isSeedingNeeded(),
        "Should propagate database exceptions to caller for proper error handling");

    verify(seasonChampionRepository).count();
    // Race winner repository should NOT be called since season champion repository throws first
    verify(raceWinnerRepository, never()).count();
  }

  @Test
  void whenRepositoriesHaveLargeDatasets_thenSeedingNotNeeded() {
    // Arrange - testing with realistic large datasets
    when(seasonChampionRepository.count()).thenReturn(75L); // Many season champions
    when(raceWinnerRepository.count()).thenReturn(5000L); // Many race results

    // Act
    boolean result = seedingStrategy.isSeedingNeeded();

    // Assert
    assertFalse(result, "Should not need seeding when repositories have large datasets");
    verify(seasonChampionRepository).count();
    verify(raceWinnerRepository).count();
  }

  @Test
  void whenCalledMultipleTimes_thenRepositoriesQueriedEachTime() {
    // Arrange
    when(seasonChampionRepository.count()).thenReturn(10L).thenReturn(20L);
    when(raceWinnerRepository.count()).thenReturn(100L).thenReturn(200L);

    // Act
    boolean firstResult = seedingStrategy.isSeedingNeeded();
    boolean secondResult = seedingStrategy.isSeedingNeeded();

    // Assert
    assertFalse(firstResult, "First call should indicate no seeding needed");
    assertFalse(secondResult, "Second call should indicate no seeding needed");
    verify(seasonChampionRepository, times(2)).count();
    verify(raceWinnerRepository, times(2)).count();
  }

  @Test
  void whenDatabaseStateChanges_thenReflectCurrentState() {
    // Arrange - simulate database state changing between calls
    when(seasonChampionRepository.count()).thenReturn(0L).thenReturn(10L);
    when(raceWinnerRepository.count()).thenReturn(0L).thenReturn(100L);

    // Act
    boolean resultWhenEmpty = seedingStrategy.isSeedingNeeded();
    boolean resultWhenPopulated = seedingStrategy.isSeedingNeeded();

    // Assert
    assertTrue(resultWhenEmpty, "Should need seeding when database is empty");
    assertFalse(resultWhenPopulated, "Should not need seeding when database is populated");
    verify(seasonChampionRepository, times(2)).count();
    verify(raceWinnerRepository, times(2)).count();
  }
}
