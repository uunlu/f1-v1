package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SeasonStatusServiceTest {

  @InjectMocks private SeasonStatusService seasonStatusService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(seasonStatusService, "totalRacesInSeason", 23);
    ReflectionTestUtils.setField(seasonStatusService, "seasonEndMonth", 12);
  }

  @Test
  void whenSeasonIsInPast_thenReturnConcluded() {
    // Arrange
    final int currentYear = Year.now().getValue();
    final String pastSeason = String.valueOf(currentYear - 1);

    // Act
    final boolean result = seasonStatusService.isSeasonConcluded(pastSeason, 15);

    // Assert
    assertTrue(result);
  }

  @Test
  void whenSeasonIsInFuture_thenReturnNotConcluded() {
    // Arrange
    final int currentYear = Year.now().getValue();
    final String futureSeason = String.valueOf(currentYear + 1);

    // Act
    final boolean result = seasonStatusService.isSeasonConcluded(futureSeason, 15);

    // Assert
    assertFalse(result);
  }

  @Test
  void whenCurrentSeasonAndAllRacesCompleted_thenReturnConcluded() {
    // Arrange
    final int currentYear = Year.now().getValue();
    final String currentSeason = String.valueOf(currentYear);

    // Act
    final boolean result = seasonStatusService.isSeasonConcluded(currentSeason, 23);

    // Assert
    assertTrue(result);
  }

  @Test
  void whenCurrentSeasonAndNotAllRacesCompleted_thenReturnNotConcluded() {
    // Arrange
    final int currentYear = Year.now().getValue();
    final String currentSeason = String.valueOf(currentYear);

    // Act
    final boolean result = seasonStatusService.isSeasonConcluded(currentSeason, 15);

    // Assert - depends on current month, but if we're before December and races < 23, should be
    // false
    final int currentMonth = LocalDate.now().getMonthValue();
    if (currentMonth < 12) {
      assertFalse(result);
    }
  }

  @Test
  void whenCurrentSeasonAndPastEndMonth_thenReturnConcluded() {
    // Arrange
    final int currentYear = Year.now().getValue();
    final String currentSeason = String.valueOf(currentYear);

    // Act - simulate being in December or later
    final int currentMonth = LocalDate.now().getMonthValue();
    final boolean result = seasonStatusService.isSeasonConcluded(currentSeason, 10);

    // Assert
    if (currentMonth >= 12) {
      assertTrue(result);
    } else {
      // If we're before December and only 10 races completed, should be false
      assertFalse(result);
    }
  }

  @Test
  void whenCurrentSeasonWithCustomConfiguration_thenUseConfigValues() {
    // Arrange
    ReflectionTestUtils.setField(seasonStatusService, "totalRacesInSeason", 20);
    ReflectionTestUtils.setField(seasonStatusService, "seasonEndMonth", 11);

    final int currentYear = Year.now().getValue();
    final String currentSeason = String.valueOf(currentYear);

    // Act
    final boolean resultWithAllRaces = seasonStatusService.isSeasonConcluded(currentSeason, 20);
    final boolean resultWithFewerRaces = seasonStatusService.isSeasonConcluded(currentSeason, 15);

    // Assert
    assertTrue(resultWithAllRaces); // All 20 races completed

    final int currentMonth = LocalDate.now().getMonthValue();
    if (currentMonth >= 11) {
      assertTrue(resultWithFewerRaces); // Past end month (November)
    } else {
      assertFalse(resultWithFewerRaces); // Before end month and not all races completed
    }
  }

  @Test
  void whenSeasonStringIsValid_thenParseCorrectly() {
    // Arrange
    final String season2020 = "2020";
    final String season2025 = "2025";

    // Act
    final boolean past = seasonStatusService.isSeasonConcluded(season2020, 17);
    final boolean future = seasonStatusService.isSeasonConcluded(season2025, 17);

    // Assert
    assertTrue(past);
    assertFalse(future);
  }
}
