package com.f1.seasonchampions.service.racewinner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.Race;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.dto.ResultsByYearResponseMRData;
import com.f1.seasonchampions.dto.ResultsByYearResponseMRDataRaceTable;
import com.f1.seasonchampions.model.RaceWinner;
import java.util.Collections;
import java.util.List;

import com.f1.seasonchampions.service.seed.racewinner.RemoteRaceWinnerSeedService;
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
class RemoteRaceWinnerServiceTest {
  @Mock private RestTemplate restTemplate;

  @InjectMocks private RemoteRaceWinnerSeedService service;

  private ResultsByYearResponse mockResponse;
  private Race mockRace;
  private com.f1.seasonchampions.dto.Result mockResult;
  private com.f1.seasonchampions.dto.Driver mockDriver;
  private com.f1.seasonchampions.dto.Constructor mockConstructor;

  @BeforeEach
  void setUp() {
    service.init(); // Initialize rate limiter
    ReflectionTestUtils.setField(service, "apiBaseUrl", "https://api.test.com/ergast/f1");

    mockDriver = new com.f1.seasonchampions.dto.Driver();
    mockDriver.setDriverId("max_verstappen");
    mockDriver.setGivenName("Max");
    mockDriver.setFamilyName("Verstappen");
    mockDriver.setNationality("Dutch");
    mockDriver.setCode("VER");

    mockConstructor = new com.f1.seasonchampions.dto.Constructor();
    mockConstructor.setConstructorId("red_bull");
    mockConstructor.setName("Red Bull Racing");
    mockConstructor.setNationality("Austrian");

    mockResult = new com.f1.seasonchampions.dto.Result();
    mockResult.setDriver(mockDriver);
    mockResult.setConstructor(mockConstructor);

    mockRace = new Race();
    mockRace.setSeason("2023");
    mockRace.setRound("1");
    mockRace.setResults(Collections.singletonList(mockResult));

    ResultsByYearResponseMRDataRaceTable raceTable = new ResultsByYearResponseMRDataRaceTable();
    raceTable.setRaces(Collections.singletonList(mockRace));

    ResultsByYearResponseMRData mrData = new ResultsByYearResponseMRData();
    mrData.setTotal("1");
    mrData.setRaceTable(raceTable);

    mockResponse = new ResultsByYearResponse();
    mockResponse.setMrData(mrData);
  }

  @Test
  void whenGettingRaceWinners_thenReturnMappedResults() {
    when(restTemplate.getForEntity(contains("/2023/results.json"), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(mockResponse));

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertEquals(1, result.size());
    RaceWinner winner = result.get(0);
    assertEquals("2023", winner.getSeason());
    assertEquals("1", winner.getRound());
    assertEquals("max_verstappen", winner.getDriver().getDriverId());
    assertEquals("red_bull", winner.getConstructor().getConstructorId());
  }

  @Test
  void whenApiCallFails_thenReturnEmptyList() {
    when(restTemplate.getForEntity(contains("/2023/results.json"), eq(ResultsByYearResponse.class)))
        .thenThrow(new RestClientException("API Error"));

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertTrue(result.isEmpty());
  }

  @Test
  void whenResponseIsNull_thenReturnEmptyList() {
    when(restTemplate.getForEntity(contains("/2023/results.json"), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(null));

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertTrue(result.isEmpty());
  }

  @Test
  void whenResponseHasMultiplePages_thenFetchAll() {
    // First page response
    ResultsByYearResponseMRData firstPageData = new ResultsByYearResponseMRData();
    firstPageData.setTotal("2");
    ResultsByYearResponseMRDataRaceTable firstPageTable =
        new ResultsByYearResponseMRDataRaceTable();
    Race firstRace = mockRace;
    firstPageTable.setRaces(Collections.singletonList(firstRace));
    firstPageData.setRaceTable(firstPageTable);
    ResultsByYearResponse firstPageResponse = new ResultsByYearResponse();
    firstPageResponse.setMrData(firstPageData);

    // Second page response
    ResultsByYearResponseMRData secondPageData = new ResultsByYearResponseMRData();
    secondPageData.setTotal("2");
    ResultsByYearResponseMRDataRaceTable secondPageTable =
        new ResultsByYearResponseMRDataRaceTable();
    Race secondRace = new Race();
    secondRace.setSeason("2023");
    secondRace.setRound("2");
    secondRace.setResults(Collections.singletonList(mockResult));
    secondPageTable.setRaces(Collections.singletonList(secondRace));
    secondPageData.setRaceTable(secondPageTable);
    ResultsByYearResponse secondPageResponse = new ResultsByYearResponse();
    secondPageResponse.setMrData(secondPageData);

    when(restTemplate.getForEntity(contains("offset=0"), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(firstPageResponse));
    when(restTemplate.getForEntity(contains("offset=1"), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(secondPageResponse));

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertEquals(2, result.size());
    assertEquals("1", result.get(0).getRound());
    assertEquals("2", result.get(1).getRound());
  }

  @Test
  void whenSavingRaceWinner_thenReturnAsIs() {
    RaceWinner winner = new RaceWinner();
    winner.setSeason("2023");
    winner.setRound("1");

    RaceWinner result = service.saveRaceWinner(winner);

    assertEquals(winner, result);
    verifyNoInteractions(restTemplate);
  }

  @Test
  void whenCheckingCompleteData_thenReturnFalse() {
    assertFalse(service.hasCompleteDataForYear(2023));
    verifyNoInteractions(restTemplate);
  }
}
