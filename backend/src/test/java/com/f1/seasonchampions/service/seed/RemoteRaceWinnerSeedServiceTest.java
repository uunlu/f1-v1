package com.f1.seasonchampions.service.seed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.*;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.service.RateLimitedApiClientService;
import com.f1.seasonchampions.service.seed.racewinner.RemoteRaceWinnerSeedService;
import java.util.ArrayList;
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
    when(rateLimitedApiClient.executeRateLimitedRequest(eq(url), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertEquals(1, winners.size());
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
  void whenMultiplePages_thenFetchAllResults() {
    // First page response - return 30 races to avoid early exit
    ResultsByYearResponseMRData firstPageData = new ResultsByYearResponseMRData();
    firstPageData.setTotal("32"); // Set total to require multiple pages
    ResultsByYearResponseMRDataRaceTable firstPageTable =
        new ResultsByYearResponseMRDataRaceTable();

    // Create 30 races for first page (equal to limit)
    List<Race> firstPageRaces = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      Race race = new Race();
      race.setSeason("2023");
      race.setRound(String.valueOf(i));
      race.setTime("15:00");

      var result = new Result();
      result.setPosition("1");

      var resultTime = new ResultTime();
      resultTime.setTime("1:30." + String.format("%03d", i));
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
      race.setResults(List.of(result));

      firstPageRaces.add(race);
    }

    firstPageTable.setRaces(firstPageRaces);
    firstPageData.setRaceTable(firstPageTable);

    var firstPageResponse = new ResultsByYearResponse();
    firstPageResponse.setMrData(firstPageData);

    // Second page response - return 2 races (less than limit, will trigger end)
    ResultsByYearResponseMRData secondPageData = new ResultsByYearResponseMRData();
    secondPageData.setTotal("32"); // Same total as first page
    ResultsByYearResponseMRDataRaceTable secondPageTable =
        new ResultsByYearResponseMRDataRaceTable();

    // Create 2 races for second page
    List<Race> secondPageRaces = new ArrayList<>();
    for (int i = 31; i <= 32; i++) {
      Race race = new Race();
      race.setSeason("2023");
      race.setRound(String.valueOf(i));
      race.setTime("15:00");

      var result = new Result();
      result.setPosition("1");

      var resultTime = new ResultTime();
      resultTime.setTime("1:29." + String.format("%03d", i));
      result.setTime(resultTime);

      var constructor = new com.f1.seasonchampions.dto.Constructor();
      constructor.setConstructorId("ferrari");
      constructor.setName("Ferrari");
      constructor.setNationality("Italian");

      var driver = new com.f1.seasonchampions.dto.Driver();
      driver.setDriverId("charles_leclerc");
      driver.setPermanentNumber("16");
      driver.setCode("LEC");
      driver.setGivenName("Charles");
      driver.setFamilyName("Leclerc");
      driver.setDateOfBirth("1997-10-16");
      driver.setNationality("Monégasque");

      result.setConstructor(constructor);
      result.setDriver(driver);
      race.setResults(List.of(result));

      secondPageRaces.add(race);
    }

    secondPageTable.setRaces(secondPageRaces);
    secondPageData.setRaceTable(secondPageTable);

    var secondPageResponse = new ResultsByYearResponse();
    secondPageResponse.setMrData(secondPageData);

    // Mock both page requests with exact URLs
    String firstPageUrl = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=0";
    String secondPageUrl = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=30";

    when(rateLimitedApiClient.executeRateLimitedRequest(
            eq(firstPageUrl), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(firstPageResponse));

    when(rateLimitedApiClient.executeRateLimitedRequest(
            eq(secondPageUrl), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(secondPageResponse));

    List<RaceWinner> result = service.getRaceWinners(2023);

    // Should get 32 race winners total (30 from first page + 2 from second page)
    assertEquals(32, result.size());
    assertEquals("1", result.get(0).getRound());
    assertEquals("30", result.get(29).getRound());
    assertEquals("31", result.get(30).getRound());
    assertEquals("32", result.get(31).getRound());

    // Verify both pages were requested with exact URLs
    verify(rateLimitedApiClient)
        .executeRateLimitedRequest(eq(firstPageUrl), eq(ResultsByYearResponse.class));
    verify(rateLimitedApiClient)
        .executeRateLimitedRequest(eq(secondPageUrl), eq(ResultsByYearResponse.class));
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
