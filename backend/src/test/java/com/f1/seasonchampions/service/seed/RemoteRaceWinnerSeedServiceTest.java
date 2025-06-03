package com.f1.seasonchampions.service.seed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.Race;
import com.f1.seasonchampions.dto.Result;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.dto.ResultsByYearResponseMRData;
import com.f1.seasonchampions.dto.ResultsByYearResponseMRDataRaceTable;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.service.seed.racewinner.RemoteRaceWinnerSeedService;
import java.util.Collections;
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

  @InjectMocks private RemoteRaceWinnerSeedService service;

  private Race mockRace;

  @BeforeEach
  void setUp() {
    service.init(); // manually call PostConstruct
    ReflectionTestUtils.setField(service, "apiBaseUrl", "https://api.test.com/ergast/f1");

    // Setup mock race data
    mockRace = new Race();
    mockRace.setSeason("2023");
    mockRace.setRound("1");
    mockRace.setTime("15:00");

    var result = new Result();
    result.setPosition("1");

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
    when(restTemplate.getForEntity(url, ResultsByYearResponse.class))
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
    when(restTemplate.getForEntity(url, ResultsByYearResponse.class))
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
    when(restTemplate.getForEntity(url, ResultsByYearResponse.class))
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
    when(restTemplate.getForEntity(url, ResultsByYearResponse.class))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    List<RaceWinner> winners = service.getRaceWinners(2023);

    // Assert
    assertTrue(winners.isEmpty());
  }

  @Test
  void whenMultiplePages_thenFetchAllResults() {
    // First page response
    ResultsByYearResponseMRData firstPageData = new ResultsByYearResponseMRData();
    firstPageData.setTotal("60"); // Set total to require multiple pages
    ResultsByYearResponseMRDataRaceTable firstPageTable =
        new ResultsByYearResponseMRDataRaceTable();
    Race firstRace = mockRace; // This already has position="1" from setUp
    firstPageTable.setRaces(Collections.singletonList(firstRace));
    firstPageData.setRaceTable(firstPageTable);

    var firstPageResponse = new ResultsByYearResponse();
    firstPageResponse.setMrData(firstPageData);

    // Second page response with its own unique race and result
    ResultsByYearResponseMRData secondPageData = new ResultsByYearResponseMRData();
    secondPageData.setTotal("60"); // Same total as first page
    ResultsByYearResponseMRDataRaceTable secondPageTable =
        new ResultsByYearResponseMRDataRaceTable();

    // Create a new race with its own result set
    Race secondRace = new Race();
    secondRace.setSeason("2023");
    secondRace.setRound("2");
    secondRace.setTime("15:00");

    // Create a new result for the second race
    var secondResult = new Result();
    secondResult.setPosition("1");

    var secondConstructor = new com.f1.seasonchampions.dto.Constructor();
    secondConstructor.setConstructorId("red_bull");
    secondConstructor.setName("Red Bull Racing");
    secondConstructor.setNationality("Austrian");

    var secondDriver = new com.f1.seasonchampions.dto.Driver();
    secondDriver.setDriverId("max_verstappen");
    secondDriver.setPermanentNumber("33");
    secondDriver.setCode("VER");
    secondDriver.setGivenName("Max");
    secondDriver.setFamilyName("Verstappen");
    secondDriver.setDateOfBirth("1997-09-30");
    secondDriver.setNationality("Dutch");

    secondResult.setConstructor(secondConstructor);
    secondResult.setDriver(secondDriver);
    secondRace.setResults(List.of(secondResult));

    secondPageTable.setRaces(Collections.singletonList(secondRace));
    secondPageData.setRaceTable(secondPageTable);

    var secondPageResponse = new ResultsByYearResponse();
    secondPageResponse.setMrData(secondPageData);

    // Mock both page requests with exact URLs
    String firstPageUrl = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=0";
    String secondPageUrl = "https://api.test.com/ergast/f1/2023/results.json?limit=30&offset=30";

    when(restTemplate.getForEntity(eq(firstPageUrl), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(firstPageResponse));

    when(restTemplate.getForEntity(eq(secondPageUrl), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(secondPageResponse));

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertEquals(2, result.size());
    assertEquals("1", result.get(0).getRound());
    assertEquals("2", result.get(1).getRound());

    // Verify both pages were requested with exact URLs
    verify(restTemplate).getForEntity(eq(firstPageUrl), eq(ResultsByYearResponse.class));
    verify(restTemplate).getForEntity(eq(secondPageUrl), eq(ResultsByYearResponse.class));
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
    verifyNoInteractions(restTemplate);
  }

  @Test
  void whenCheckingCompleteData_thenReturnFalse() {
    // Remote service always returns false for hasCompleteData
    assertFalse(service.hasCompleteDataForYear(2023));
    verifyNoInteractions(restTemplate);
  }
}
