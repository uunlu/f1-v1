package com.f1.seasonchampions.service.seed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.*;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.service.RateLimitedApiClientService;
import com.f1.seasonchampions.service.seed.racewinner.RemoteRaceWinnerSeedService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RemoteRaceWinnerSeedServiceTest {

  @Mock private RestTemplate restTemplate;
  @Mock private RateLimitedApiClientService rateLimitedApiClient;

  @InjectMocks private RemoteRaceWinnerSeedService service;

  private Race mockRace;

  @BeforeEach
  void setUp() {
    // Rate limiter is now centralized and automatically initialized
    ReflectionTestUtils.setField(service, "apiBaseUrl", "https://api.test.com/ergast/f1");

    // Setup mock race data
    mockRace = new Race();
    mockRace.setSeason("2023");
    mockRace.setRound("1");
    mockRace.setTime("15:00");

    var result = new Result();
    result.setPosition("1");

    var resultTime = new ResultTime();
    resultTime.setTime("1:30.123"); // Add the missing time value
    result.setTime(resultTime);

    var constructor = new com.f1.seasonchampions.dto.Constructor();
    constructor.setConstructorId("red_bull");
    constructor.setName("Red Bull Racing");
    constructor.setNationality("Austrian");

    var driver = new com.f1.seasonchampions.dto.Driver();
    driver.setDriverId("max_verstappen");
    driver.setPermanentNumber("33");
    driver.setCode("VER");
    driver.setGivenName("Max");
    driver.setFamilyName("Verstappen");
    driver.setDateOfBirth("1997-09-30");
    driver.setNationality("Dutch");

    result.setConstructor(constructor);
    result.setDriver(driver);

    mockRace.setResults(List.of(result));
  }

  @Test
  void whenFetchingRaceWinners_thenReturnMappedResults() {
    // Arrange
    var response = new ResultsByYearResponse();
    var mrData = new ResultsByYearResponseMRData();
    mrData.setTotal("1");
    var raceTable = new ResultsByYearResponseMRDataRaceTable();
    raceTable.setRaces(List.of(mockRace));
    mrData.setRaceTable(raceTable);
    response.setMrData(mrData);

    String url = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=0";
    when(rateLimitedApiClient.executeRateLimitedRequest(any(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertEquals(30, winners.size());
    RaceWinner winner = winners.get(0);
    assertEquals("2023", winner.getSeason());
    assertEquals("1", winner.getRound());
    assertEquals("max_verstappen", winner.getDriver().getDriverId());
    assertEquals("red_bull", winner.getConstructor().getConstructorId());
  }

  @Test
  void whenApiCallFails_thenReturnEmptyList() {
    // Arrange
    String url = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=0";
    when(rateLimitedApiClient.executeRateLimitedRequest(eq(url), eq(ResultsByYearResponse.class)))
        .thenThrow(new RestClientException("API Error"));

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertTrue(winners.isEmpty());
  }

  @Test
  void whenResponseIsNull_thenReturnEmptyList() {
    // Arrange
    String url = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=0";
    when(rateLimitedApiClient.executeRateLimitedRequest(eq(url), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(null));

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertTrue(winners.isEmpty());
  }

  @Test
  void whenTotalIsInvalid_thenReturnEmptyList() {
    // Arrange
    var response = new ResultsByYearResponse();
    var mrData = new ResultsByYearResponseMRData();
    mrData.setTotal("invalid");
    response.setMrData(mrData);

    String url = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=0";
    when(rateLimitedApiClient.executeRateLimitedRequest(eq(url), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertTrue(winners.isEmpty());
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
    verifyNoInteractions(rateLimitedApiClient);
  }

  @Test
  void whenCheckingCompleteData_thenReturnFalse() {
    // Remote service always returns false for hasCompleteData
    assertFalse(service.hasCompleteDataForYear(2023));
    verifyNoInteractions(rateLimitedApiClient);
  }
}
