package com.f1.seasonchampions.service.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.*;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class F1DataServiceTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock private RestClient.RequestHeadersSpec requestHeadersSpec;
  @Mock private RestClient.ResponseSpec responseSpec;
  @Mock private RaceWinnerRepository raceWinnerRepository;

  @InjectMocks private F1DataService f1DataService;

  private final LocalDate cutoffDate = LocalDate.of(2024, 6, 1);
  private final int testYear = 2024;

  @BeforeEach
  void setUp() {
    // Set the baseUrl field since it's injected via @Value
    ReflectionTestUtils.setField(f1DataService, "baseUrl", "https://api.test.com/ergast/f1");

    // Setup the RestClient mock chain
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void whenNewRaceDataAvailable_thenReturnTrueAndSaveData() {
    // Arrange
    ResultsByYearResponse response = createValidResponse();
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(response);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertTrue(result, "Should return true when new race data is found");

    ArgumentCaptor<RaceWinner> raceWinnerCaptor = ArgumentCaptor.forClass(RaceWinner.class);
    verify(raceWinnerRepository, times(2)).save(raceWinnerCaptor.capture());

    List<RaceWinner> savedWinners = raceWinnerCaptor.getAllValues();
    assertEquals(2, savedWinners.size());
    assertEquals("2024", savedWinners.get(0).getSeason());
    assertEquals("max_verstappen", savedWinners.get(0).getDriver().getDriverId());
    assertEquals("red_bull", savedWinners.get(0).getConstructor().getConstructorId());
  }

  @Test
  void whenNoNewRaceDataAfterCutoffDate_thenReturnFalse() {
    // Arrange - races before cutoff date
    ResultsByYearResponse response = createResponseWithOldRaces();
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(response);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertFalse(result, "Should return false when no new race data after cutoff date");
    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenApiReturnsNullResponse_thenReturnFalse() {
    // Arrange
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(null);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertFalse(result, "Should return false when API returns null");
    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenApiReturnsEmptyMrData_thenReturnFalse() {
    // Arrange
    ResultsByYearResponse response = new ResultsByYearResponse();
    response.setMrData(null);
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(response);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertFalse(result, "Should return false when MrData is null");
    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenApiReturnsEmptyRaceTable_thenReturnFalse() {
    // Arrange
    ResultsByYearResponse response = new ResultsByYearResponse();
    ResultsByYearResponseMRData mrData = new ResultsByYearResponseMRData();
    mrData.setRaceTable(null);
    response.setMrData(mrData);
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(response);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertFalse(result, "Should return false when RaceTable is null");
    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenRaceHasNoResults_thenSkipThatRace() {
    // Arrange
    ResultsByYearResponse response = createResponseWithRaceWithoutResults();
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(response);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertTrue(result, "Should return true for the race with results");
    verify(raceWinnerRepository, times(1)).save(any()); // Only one race saved
  }

  @Test
  void whenApiThrowsException_thenReturnFalseAndHandleGracefully() {
    // Arrange
    when(responseSpec.body(ResultsByYearResponse.class))
        .thenThrow(new RestClientException("API Error"));

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertFalse(result, "Should return false when API throws exception");
    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenApiReturnsPartialData_thenSaveOnlyValidRaces() {
    // Arrange
    ResultsByYearResponse response = createMixedValidityResponse();
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(response);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertTrue(result, "Should return true when at least one valid race found");
    verify(raceWinnerRepository, times(1)).save(any()); // Only valid race saved
  }

  @Test
  void whenMultipleResultsInRace_thenSaveAllResults() {
    // Arrange
    ResultsByYearResponse response = createResponseWithMultipleResults();
    when(responseSpec.body(ResultsByYearResponse.class)).thenReturn(response);

    // Act
    boolean result = f1DataService.fetchAndStoreNewRaceWinners(testYear, cutoffDate);

    // Assert
    assertTrue(result, "Should return true when race data is found");
    verify(raceWinnerRepository, times(3)).save(any()); // All 3 results saved
  }

  // Helper methods to create test data
  private ResultsByYearResponse createValidResponse() {
    ResultsByYearResponse response = new ResultsByYearResponse();
    ResultsByYearResponseMRData mrData = new ResultsByYearResponseMRData();
    ResultsByYearResponseMRDataRaceTable raceTable = new ResultsByYearResponseMRDataRaceTable();

    List<Race> races =
        List.of(
            createRace("2024-06-15", "Spanish Grand Prix"),
            createRace("2024-06-10", "Canadian Grand Prix"));

    raceTable.setRaces(races);
    mrData.setRaceTable(raceTable);
    response.setMrData(mrData);

    return response;
  }

  private ResultsByYearResponse createResponseWithOldRaces() {
    ResultsByYearResponse response = new ResultsByYearResponse();
    ResultsByYearResponseMRData mrData = new ResultsByYearResponseMRData();
    ResultsByYearResponseMRDataRaceTable raceTable = new ResultsByYearResponseMRDataRaceTable();

    List<Race> races =
        List.of(
            createRace("2024-05-15", "Monaco Grand Prix"), // Before cutoff
            createRace("2024-05-20", "Emilia Romagna Grand Prix") // Before cutoff
            );

    raceTable.setRaces(races);
    mrData.setRaceTable(raceTable);
    response.setMrData(mrData);

    return response;
  }

  private ResultsByYearResponse createResponseWithRaceWithoutResults() {
    ResultsByYearResponse response = new ResultsByYearResponse();
    ResultsByYearResponseMRData mrData = new ResultsByYearResponseMRData();
    ResultsByYearResponseMRDataRaceTable raceTable = new ResultsByYearResponseMRDataRaceTable();

    Race raceWithResults = createRace("2024-06-15", "Spanish Grand Prix");

    Race raceWithoutResults = new Race();
    raceWithoutResults.setDate("2024-06-20");
    raceWithoutResults.setRaceName("Austrian Grand Prix");
    raceWithoutResults.setSeason("2024");
    raceWithoutResults.setTime("15:00:00Z");
    raceWithoutResults.setResults(null); // No results

    raceTable.setRaces(List.of(raceWithResults, raceWithoutResults));
    mrData.setRaceTable(raceTable);
    response.setMrData(mrData);

    return response;
  }

  private ResultsByYearResponse createMixedValidityResponse() {
    ResultsByYearResponse response = new ResultsByYearResponse();
    ResultsByYearResponseMRData mrData = new ResultsByYearResponseMRData();
    ResultsByYearResponseMRDataRaceTable raceTable = new ResultsByYearResponseMRDataRaceTable();

    Race validRace = createRace("2024-06-15", "Spanish Grand Prix");
    Race invalidRace = createRace("2024-05-15", "Monaco Grand Prix"); // Before cutoff

    raceTable.setRaces(List.of(validRace, invalidRace));
    mrData.setRaceTable(raceTable);
    response.setMrData(mrData);

    return response;
  }

  private ResultsByYearResponse createResponseWithMultipleResults() {
    ResultsByYearResponse response = new ResultsByYearResponse();
    ResultsByYearResponseMRData mrData = new ResultsByYearResponseMRData();
    ResultsByYearResponseMRDataRaceTable raceTable = new ResultsByYearResponseMRDataRaceTable();

    Race race = new Race();
    race.setDate("2024-06-15");
    race.setRaceName("Spanish Grand Prix");
    race.setSeason("2024");
    race.setTime("15:00:00Z");

    // Create multiple results for the race
    Result result1 = createResult("1", "max_verstappen", "red_bull");
    Result result2 = createResult("2", "charles_leclerc", "ferrari");
    Result result3 = createResult("3", "lando_norris", "mclaren");

    race.setResults(List.of(result1, result2, result3));

    raceTable.setRaces(List.of(race));
    mrData.setRaceTable(raceTable);
    response.setMrData(mrData);

    return response;
  }

  private Race createRace(String date, String raceName) {
    Race race = new Race();
    race.setDate(date);
    race.setRaceName(raceName);
    race.setSeason("2024");
    race.setTime("15:00:00Z");

    Result result = createResult("1", "max_verstappen", "red_bull");
    race.setResults(List.of(result));

    return race;
  }

  private Result createResult(String position, String driverId, String constructorId) {
    Result result = new Result();
    result.setPosition(position);

    Driver driver = new Driver();
    driver.setDriverId(driverId);
    driver.setCode("VER");
    driver.setGivenName("Max");
    driver.setFamilyName("Verstappen");
    driver.setNationality("Dutch");
    driver.setDateOfBirth("1997-09-30");
    driver.setPermanentNumber("1");
    result.setDriver(driver);

    Constructor constructor = new Constructor();
    constructor.setConstructorId(constructorId);
    constructor.setName("Red Bull Racing");
    constructor.setNationality("Austrian");
    result.setConstructor(constructor);

    return result;
  }
}
