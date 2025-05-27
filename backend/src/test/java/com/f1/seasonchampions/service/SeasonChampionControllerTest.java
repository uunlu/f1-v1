package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.controller.SeasonChampionController;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.service.racewinner.RaceWinnerService;
import com.f1.seasonchampions.service.seasonchampion.SeasonChampionService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SeasonChampionControllerTest {

  private SeasonChampionService seasonChampionService;
  private RaceWinnerService raceWinnerService;
  private SeasonChampionController controller;

  @BeforeEach
  void setUp() {
    seasonChampionService = mock(SeasonChampionService.class);
    raceWinnerService = mock(RaceWinnerService.class);
    controller = new SeasonChampionController(seasonChampionService, raceWinnerService);
  }

  @Test
  void getSeasonChampions_returnsListFromService() {
    // Arrange
    var mockChampion = new SeasonChampion();
    when(seasonChampionService.getSeasonChampions(new SeasonRangeRequest(2005, 2024)))
        .thenReturn(List.of(mockChampion));

    // Act
    ResponseEntity<List<SeasonChampion>> response = controller.getSeasonChampions(2005, 2024);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(seasonChampionService).getSeasonChampions(new SeasonRangeRequest(2005, 2024));
  }

  @Test
  void getRaceResults_returnsListFromService() {
    // Arrange
    var mockWinner = new RaceWinner();
    when(raceWinnerService.getRaceWinners(2023)).thenReturn(List.of(mockWinner));

    // Act
    ResponseEntity<List<RaceWinner>> response = controller.getRaceResults(2023);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(raceWinnerService).getRaceWinners(2023);
  }
}
