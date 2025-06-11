package com.f1.seasonchampions.service.seed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.service.F1RaceDataFetchingService;
import com.f1.seasonchampions.service.seed.racewinner.RemoteRaceWinnerSeedService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemoteRaceWinnerSeedServiceTest {

  @Mock private F1RaceDataFetchingService f1RaceDataFetchingService;

  @InjectMocks private RemoteRaceWinnerSeedService service;

  private RaceWinner mockRaceWinner;

  @BeforeEach
  void setUp() {
    // Setup mock race winner data
    mockRaceWinner = new RaceWinner();
    mockRaceWinner.setSeason("2023");
    mockRaceWinner.setRound("1");
    mockRaceWinner.setTime("1:30.123");

    var constructor = new Constructor();
    constructor.setConstructorId("red_bull");
    constructor.setName("Red Bull Racing");
    constructor.setNationality("Austrian");

    var driver = new Driver();
    driver.setDriverId("max_verstappen");
    driver.setPermanentNumber("33");
    driver.setCode("VER");
    driver.setGivenName("Max");
    driver.setFamilyName("Verstappen");
    driver.setDateOfBirth("1997-09-30");
    driver.setNationality("Dutch");

    mockRaceWinner.setConstructor(constructor);
    mockRaceWinner.setDriver(driver);
  }

  @Test
  void whenFetchingRaceWinners_thenReturnResultsFromSharedService() {
    // Arrange
    List<RaceWinner> expectedWinners = List.of(mockRaceWinner);
    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(2023)).thenReturn(expectedWinners);

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertEquals(expectedWinners, winners);
    verify(f1RaceDataFetchingService).fetchRaceWinnersForYear(2023);
  }

  @Test
  void whenSharedServiceThrowsException_thenReturnEmptyList() {
    // Arrange
    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(2023))
        .thenThrow(new RuntimeException("API Error"));

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertTrue(winners.isEmpty());
    verify(f1RaceDataFetchingService).fetchRaceWinnersForYear(2023);
  }

  @Test
  void whenSharedServiceReturnsNull_thenReturnEmptyList() {
    // Arrange
    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(2023)).thenReturn(null);

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertTrue(winners.isEmpty());
    verify(f1RaceDataFetchingService).fetchRaceWinnersForYear(2023);
  }

  @Test
  void whenSavingRaceWinner_thenReturnAsIs() {
    // Arrange
    RaceWinner winner = new RaceWinner();
    winner.setSeason("2023");
    winner.setRound("1");

    // Act
    RaceWinner result = service.saveRaceWinner(winner);

    // Assert
    assertEquals(winner, result);
    verifyNoInteractions(f1RaceDataFetchingService);
  }

  @Test
  void whenCheckingCompleteData_thenReturnFalse() {
    // Remote service always returns false for hasCompleteData
    assertFalse(service.hasCompleteDataForYear(2023));
    verifyNoInteractions(f1RaceDataFetchingService);
  }
}
