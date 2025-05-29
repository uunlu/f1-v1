package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.controller.SeedDataController;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.service.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seasonchampion.SeasonChampionSeedService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SeedDataControllerTest {

  private SeasonChampionSeedService seasonChampionSeedService;
  private RaceWinnerSeedService raceWinnerSeedService;
  private SeedDataController controller;

  @BeforeEach
  void setUp() {
    seasonChampionSeedService = mock(SeasonChampionSeedService.class);
    raceWinnerSeedService = mock(RaceWinnerSeedService.class);
    controller = new SeedDataController(seasonChampionSeedService, raceWinnerSeedService);
  }

  @Test
  void getSeasonChampions_returnsListFromService() {
    // Arrange
    var mockChampion = new SeasonChampion();
    when(seasonChampionSeedService.getSeasonChampions(new SeasonRangeRequest(2005, 2024)))
        .thenReturn(List.of(mockChampion));

    // Act
    ResponseEntity<List<SeasonChampion>> response = controller.getSeasonChampions(2005, 2024);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(seasonChampionSeedService).getSeasonChampions(new SeasonRangeRequest(2005, 2024));
  }

  @Test
  void getRaceResults_returnsListFromService() {
    // Arrange
    var mockWinner = new RaceWinner();
    when(raceWinnerSeedService.getRaceWinners(2023)).thenReturn(List.of(mockWinner));

    // Act
    ResponseEntity<List<RaceWinner>> response = controller.getRaceResults(2023);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(raceWinnerSeedService).getRaceWinners(2023);
  }
}
