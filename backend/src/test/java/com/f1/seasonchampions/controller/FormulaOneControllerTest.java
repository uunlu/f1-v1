package com.f1.seasonchampions.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.DriverInfo;
import com.f1.seasonchampions.dto.DriverInfoImpl;
import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.dto.RaceWinnerListItemImpl;
import com.f1.seasonchampions.dto.RaceWinnerSeasonResponse;
import com.f1.seasonchampions.dto.SeasonChampionListItem;
import com.f1.seasonchampions.dto.SeasonChampionListItemImpl;
import com.f1.seasonchampions.service.query.racewinner.RaceWinnerQueryService;
import com.f1.seasonchampions.service.query.seasonchampion.SeasonChampionQueryService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FormulaOneControllerTest {

  @Mock private RaceWinnerQueryService raceWinnerQueryService;
  @Mock private SeasonChampionQueryService seasonChampionQueryService;

  private FormulaOneController formulaOneController;

  @BeforeEach
  void setUp() {
    formulaOneController =
        new FormulaOneController(raceWinnerQueryService, seasonChampionQueryService);
  }

  @Test
  void getRaceWinnersBySeason_whenWinnersExist_returnsOkWithWinners() {
    // Arrange
    int season = 2023;
    DriverInfo driver = new DriverInfoImpl("Lewis", "Hamilton", "hamilton");
    RaceWinnerListItem winner =
        new RaceWinnerListItemImpl(
            "2023", "1", true, driver, "hamilton", "mercedes", "Mercedes", "1:30.123");
    List<RaceWinnerListItem> winners = List.of(winner);

    when(raceWinnerQueryService.getWinnersBySeason(season)).thenReturn(winners);

    // Act
    ResponseEntity<List<RaceWinnerListItem>> response =
        formulaOneController.getRaceWinnersBySeason(season);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("2023", response.getBody().get(0).getSeasonName());
    assertEquals("1", response.getBody().get(0).getRound());
    assertTrue(response.getBody().get(0).isChampion());
    assertEquals("Lewis", response.getBody().get(0).getDriver().getGivenName());
    assertEquals("Hamilton", response.getBody().get(0).getDriver().getFamilyName());
    assertEquals("hamilton", response.getBody().get(0).getDriver().getDriverId());

    verify(raceWinnerQueryService).getWinnersBySeason(season);
  }

  @Test
  void getRaceWinnersBySeason_whenNoWinnersExist_returnsOkWithEmptyList() {
    // Arrange
    int season = 2025;
    when(raceWinnerQueryService.getWinnersBySeason(season)).thenReturn(Collections.emptyList());

    // Act
    ResponseEntity<List<RaceWinnerListItem>> response =
        formulaOneController.getRaceWinnersBySeason(season);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());

    verify(raceWinnerQueryService).getWinnersBySeason(season);
  }

  @Test
  void getRaceWinnersBySeason_withMultipleWinners_returnsAllWinners() {
    // Arrange
    int season = 2023;
    DriverInfo driver1 = new DriverInfoImpl("Lewis", "Hamilton", "hamilton");
    DriverInfo driver2 = new DriverInfoImpl("Max", "Verstappen", "max_verstappen");

    RaceWinnerListItem winner1 =
        new RaceWinnerListItemImpl(
            "2023", "1", false, driver1, "hamilton", "mercedes", "Mercedes", "1:30.123");
    RaceWinnerListItem winner2 =
        new RaceWinnerListItemImpl(
            "2023", "2", true, driver2, "max_verstappen", "red_bull", "Red Bull", "1:29.456");
    List<RaceWinnerListItem> winners = List.of(winner1, winner2);

    when(raceWinnerQueryService.getWinnersBySeason(season)).thenReturn(winners);

    // Act
    ResponseEntity<List<RaceWinnerListItem>> response =
        formulaOneController.getRaceWinnersBySeason(season);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());

    // Verify first winner
    assertEquals("2023", response.getBody().get(0).getSeasonName());
    assertEquals("1", response.getBody().get(0).getRound());
    assertFalse(response.getBody().get(0).isChampion());
    assertEquals("Lewis", response.getBody().get(0).getDriver().getGivenName());

    // Verify second winner
    assertEquals("2023", response.getBody().get(1).getSeasonName());
    assertEquals("2", response.getBody().get(1).getRound());
    assertTrue(response.getBody().get(1).isChampion());
    assertEquals("Max", response.getBody().get(1).getDriver().getGivenName());

    verify(raceWinnerQueryService).getWinnersBySeason(season);
  }

  @Test
  void getRaceWinnersWithMetadata_whenDataExists_returnsOkWithMetadata() {
    // Arrange
    int season = 2023;
    DriverInfo driver = new DriverInfoImpl("Max", "Verstappen", "max_verstappen");
    RaceWinnerListItem winner =
        new RaceWinnerListItemImpl(
            "2023", "1", true, driver, "max_verstappen", "red_bull", "Red Bull", "1:29.456");
    List<RaceWinnerListItem> winners = List.of(winner);

    RaceWinnerSeasonResponse expectedResponse =
        new RaceWinnerSeasonResponse("2023", true, true, 22, winners);

    when(raceWinnerQueryService.getWinnersWithSeasonMetadata(season)).thenReturn(expectedResponse);

    // Act
    ResponseEntity<RaceWinnerSeasonResponse> response =
        formulaOneController.getRaceWinnersWithMetadata(season);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals("2023", response.getBody().getSeason());
    assertTrue(response.getBody().isSeasonConcluded());
    assertTrue(response.getBody().isHasChampion());
    assertEquals(22, response.getBody().getTotalRaces());
    assertEquals(1, response.getBody().getRaceWinners().size());

    verify(raceWinnerQueryService).getWinnersWithSeasonMetadata(season);
  }

  @Test
  void getRaceWinnersWithMetadata_whenSeasonNotConcluded_returnsCorrectMetadata() {
    // Arrange
    int season = 2024;
    List<RaceWinnerListItem> winners = Collections.emptyList();

    RaceWinnerSeasonResponse expectedResponse =
        new RaceWinnerSeasonResponse("2024", false, false, 0, winners);

    when(raceWinnerQueryService.getWinnersWithSeasonMetadata(season)).thenReturn(expectedResponse);

    // Act
    ResponseEntity<RaceWinnerSeasonResponse> response =
        formulaOneController.getRaceWinnersWithMetadata(season);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("2024", response.getBody().getSeason());
    assertFalse(response.getBody().isSeasonConcluded());
    assertFalse(response.getBody().isHasChampion());
    assertEquals(0, response.getBody().getTotalRaces());
    assertTrue(response.getBody().getRaceWinners().isEmpty());

    verify(raceWinnerQueryService).getWinnersWithSeasonMetadata(season);
  }

  @Test
  void getAllSeasons_whenSeasonsExist_returnsOkWithSeasons() {
    // Arrange
    SeasonChampionListItem season1 =
        new SeasonChampionListItemImpl("2022", "Max Verstappen", "Red Bull");
    SeasonChampionListItem season2 =
        new SeasonChampionListItemImpl("2023", "Max Verstappen", "Red Bull");
    List<SeasonChampionListItem> seasons = List.of(season1, season2);

    when(seasonChampionQueryService.getAllSeasons()).thenReturn(seasons);

    // Act
    ResponseEntity<List<SeasonChampionListItem>> response = formulaOneController.getAllSeasons();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());
    assertEquals("2022", response.getBody().get(0).getSeason());
    assertEquals("Max Verstappen", response.getBody().get(0).getDriver());
    assertEquals("Red Bull", response.getBody().get(0).getConstructor());
    assertEquals("2023", response.getBody().get(1).getSeason());

    verify(seasonChampionQueryService).getAllSeasons();
  }

  @Test
  void getAllSeasons_whenNoSeasonsExist_returnsOkWithEmptyList() {
    // Arrange
    when(seasonChampionQueryService.getAllSeasons()).thenReturn(Collections.emptyList());

    // Act
    ResponseEntity<List<SeasonChampionListItem>> response = formulaOneController.getAllSeasons();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());

    verify(seasonChampionQueryService).getAllSeasons();
  }

  @Test
  void getAllSeasons_withMultipleSeasons_returnsAllInOrder() {
    // Arrange
    SeasonChampionListItem season1 =
        new SeasonChampionListItemImpl("2020", "Lewis Hamilton", "Mercedes");
    SeasonChampionListItem season2 =
        new SeasonChampionListItemImpl("2021", "Max Verstappen", "Red Bull");
    SeasonChampionListItem season3 =
        new SeasonChampionListItemImpl("2022", "Max Verstappen", "Red Bull");
    List<SeasonChampionListItem> seasons = List.of(season1, season2, season3);

    when(seasonChampionQueryService.getAllSeasons()).thenReturn(seasons);

    // Act
    ResponseEntity<List<SeasonChampionListItem>> response = formulaOneController.getAllSeasons();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(3, response.getBody().size());

    // Verify order and content
    assertEquals("2020", response.getBody().get(0).getSeason());
    assertEquals("Lewis Hamilton", response.getBody().get(0).getDriver());
    assertEquals("Mercedes", response.getBody().get(0).getConstructor());

    assertEquals("2021", response.getBody().get(1).getSeason());
    assertEquals("Max Verstappen", response.getBody().get(1).getDriver());
    assertEquals("Red Bull", response.getBody().get(1).getConstructor());

    assertEquals("2022", response.getBody().get(2).getSeason());
    assertEquals("Max Verstappen", response.getBody().get(2).getDriver());
    assertEquals("Red Bull", response.getBody().get(2).getConstructor());

    verify(seasonChampionQueryService).getAllSeasons();
  }

  @Test
  void getRaceWinnersBySeason_verifyServiceInteraction() {
    // Arrange
    int season = 2023;
    when(raceWinnerQueryService.getWinnersBySeason(season)).thenReturn(Collections.emptyList());

    // Act
    formulaOneController.getRaceWinnersBySeason(season);

    // Assert
    verify(raceWinnerQueryService, times(1)).getWinnersBySeason(season);
    verifyNoMoreInteractions(raceWinnerQueryService);
    verifyNoInteractions(seasonChampionQueryService);
  }

  @Test
  void getRaceWinnersWithMetadata_verifyServiceInteraction() {
    // Arrange
    int season = 2023;
    RaceWinnerSeasonResponse expectedResponse =
        new RaceWinnerSeasonResponse("2023", true, true, 22, Collections.emptyList());
    when(raceWinnerQueryService.getWinnersWithSeasonMetadata(season)).thenReturn(expectedResponse);

    // Act
    formulaOneController.getRaceWinnersWithMetadata(season);

    // Assert
    verify(raceWinnerQueryService, times(1)).getWinnersWithSeasonMetadata(season);
    verifyNoMoreInteractions(raceWinnerQueryService);
    verifyNoInteractions(seasonChampionQueryService);
  }

  @Test
  void getAllSeasons_verifyServiceInteraction() {
    // Arrange
    when(seasonChampionQueryService.getAllSeasons()).thenReturn(Collections.emptyList());

    // Act
    formulaOneController.getAllSeasons();

    // Assert
    verify(seasonChampionQueryService, times(1)).getAllSeasons();
    verifyNoMoreInteractions(seasonChampionQueryService);
    verifyNoInteractions(raceWinnerQueryService);
  }
}
