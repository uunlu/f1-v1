package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class SeasonChampionServiceTest {

  @Mock private SeasonChampionRepository seasonChampionRepository;

  @InjectMocks private SeasonChampionService seasonChampionService;

  private Driver testDriver;
  private Constructor testConstructor;
  private RaceWinner testRaceWinner;
  private SeasonChampion testSeasonChampion;

  @BeforeEach
  void setUp() {
    testDriver = new Driver();
    testDriver.setDriverId("max_verstappen");
    testDriver.setGivenName("Max");
    testDriver.setFamilyName("Verstappen");
    testDriver.setCode("VER");
    testDriver.setPermanentNumber("1");
    testDriver.setNationality("Dutch");
    testDriver.setDateOfBirth("1997-09-30");

    testConstructor = new Constructor();
    testConstructor.setConstructorId("red_bull");
    testConstructor.setName("Red Bull Racing Honda RBPT");
    testConstructor.setNationality("Austrian");

    testRaceWinner = new RaceWinner();
    testRaceWinner.setId(1L);
    testRaceWinner.setSeason("2023");
    testRaceWinner.setRound("1");
    testRaceWinner.setDriver(testDriver);
    testRaceWinner.setConstructor(testConstructor);
    testRaceWinner.setTime("1:28.456");

    testSeasonChampion = new SeasonChampion();
    testSeasonChampion.setSeason("2023");
    testSeasonChampion.setDriver(testDriver);
    testSeasonChampion.setConstructor(testConstructor);
  }

  @Test
  void whenSaveSeasonChampion_thenReturnSavedChampion() {
    // Arrange
    String season = "2023";
    when(seasonChampionRepository.save(any(SeasonChampion.class))).thenReturn(testSeasonChampion);

    // Act
    SeasonChampion result = seasonChampionService.saveSeasonChampion(season, testRaceWinner);

    // Assert
    assertNotNull(result);
    assertEquals("2023", result.getSeason());
    assertEquals(testDriver, result.getDriver());
    assertEquals(testConstructor, result.getConstructor());

    // Verify repository interaction
    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();
    assertEquals(season, capturedChampion.getSeason());
    assertEquals(testDriver, capturedChampion.getDriver());
    assertEquals(testConstructor, capturedChampion.getConstructor());
  }

  @Test
  void whenSaveSeasonChampionWithNullDriver_thenHandleGracefully() {
    // Arrange
    String season = "2023";
    RaceWinner winnerWithNullDriver = new RaceWinner();
    winnerWithNullDriver.setId(1L);
    winnerWithNullDriver.setSeason("2023");
    winnerWithNullDriver.setRound("1");
    winnerWithNullDriver.setDriver(null);
    winnerWithNullDriver.setConstructor(testConstructor);
    winnerWithNullDriver.setTime("1:28.456");

    SeasonChampion championWithNullDriver = new SeasonChampion();
    championWithNullDriver.setSeason("2023");
    championWithNullDriver.setDriver(null);
    championWithNullDriver.setConstructor(testConstructor);

    when(seasonChampionRepository.save(any(SeasonChampion.class)))
        .thenReturn(championWithNullDriver);

    // Act
    SeasonChampion result = seasonChampionService.saveSeasonChampion(season, winnerWithNullDriver);

    // Assert
    assertNotNull(result);
    assertEquals("2023", result.getSeason());
    assertNull(result.getDriver());
    assertEquals(testConstructor, result.getConstructor());

    // Verify repository interaction
    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();
    assertEquals(season, capturedChampion.getSeason());
    assertNull(capturedChampion.getDriver());
    assertEquals(testConstructor, capturedChampion.getConstructor());
  }

  @Test
  void whenSaveSeasonChampionWithNullConstructor_thenHandleGracefully() {
    // Arrange
    String season = "2023";
    RaceWinner winnerWithNullConstructor = new RaceWinner();
    winnerWithNullConstructor.setId(1L);
    winnerWithNullConstructor.setSeason("2023");
    winnerWithNullConstructor.setRound("1");
    winnerWithNullConstructor.setDriver(testDriver);
    winnerWithNullConstructor.setConstructor(null);
    winnerWithNullConstructor.setTime("1:28.456");

    SeasonChampion championWithNullConstructor = new SeasonChampion();
    championWithNullConstructor.setSeason("2023");
    championWithNullConstructor.setDriver(testDriver);
    championWithNullConstructor.setConstructor(null);

    when(seasonChampionRepository.save(any(SeasonChampion.class)))
        .thenReturn(championWithNullConstructor);

    // Act
    SeasonChampion result =
        seasonChampionService.saveSeasonChampion(season, winnerWithNullConstructor);

    // Assert
    assertNotNull(result);
    assertEquals("2023", result.getSeason());
    assertEquals(testDriver, result.getDriver());
    assertNull(result.getConstructor());

    // Verify repository interaction
    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();
    assertEquals(season, capturedChampion.getSeason());
    assertEquals(testDriver, capturedChampion.getDriver());
    assertNull(capturedChampion.getConstructor());
  }

  @Test
  void whenSaveSeasonChampionWithDifferentSeason_thenUseProvidedSeason() {
    // Arrange
    String providedSeason = "2022";
    // Race winner has season 2023, but we provide 2022 as parameter
    when(seasonChampionRepository.save(any(SeasonChampion.class))).thenReturn(testSeasonChampion);

    // Act
    SeasonChampion result =
        seasonChampionService.saveSeasonChampion(providedSeason, testRaceWinner);

    // Assert
    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();
    assertEquals(
        providedSeason,
        capturedChampion.getSeason()); // Should use provided season, not race winner's season
    assertEquals(testDriver, capturedChampion.getDriver());
    assertEquals(testConstructor, capturedChampion.getConstructor());
  }

  @Test
  void whenRepositoryThrowsException_thenPropagateException() {
    // Arrange
    String season = "2023";
    DataAccessException dataAccessException =
        new DataAccessException("Database connection failed") {};

    when(seasonChampionRepository.save(any(SeasonChampion.class))).thenThrow(dataAccessException);

    // Act & Assert
    DataAccessException thrown =
        assertThrows(
            DataAccessException.class,
            () -> seasonChampionService.saveSeasonChampion(season, testRaceWinner));

    assertEquals("Database connection failed", thrown.getMessage());
    verify(seasonChampionRepository).save(any(SeasonChampion.class));
  }

  @Test
  void whenSaveSeasonChampionMultipleTimes_thenEachCallCreatesSeparateChampion() {
    // Arrange
    String season1 = "2022";
    String season2 = "2023";

    Driver driver2 = new Driver();
    driver2.setDriverId("lewis_hamilton");
    driver2.setGivenName("Lewis");
    driver2.setFamilyName("Hamilton");

    Constructor constructor2 = new Constructor();
    constructor2.setConstructorId("mercedes");
    constructor2.setName("Mercedes-AMG Petronas F1 Team");

    RaceWinner winner2 = new RaceWinner();
    winner2.setDriver(driver2);
    winner2.setConstructor(constructor2);

    SeasonChampion champion1 = new SeasonChampion();
    champion1.setSeason(season1);
    champion1.setDriver(testDriver);
    champion1.setConstructor(testConstructor);

    SeasonChampion champion2 = new SeasonChampion();
    champion2.setSeason(season2);
    champion2.setDriver(driver2);
    champion2.setConstructor(constructor2);

    when(seasonChampionRepository.save(any(SeasonChampion.class)))
        .thenReturn(champion1)
        .thenReturn(champion2);

    // Act
    SeasonChampion result1 = seasonChampionService.saveSeasonChampion(season1, testRaceWinner);
    SeasonChampion result2 = seasonChampionService.saveSeasonChampion(season2, winner2);

    // Assert
    assertEquals(season1, result1.getSeason());
    assertEquals(testDriver, result1.getDriver());
    assertEquals(testConstructor, result1.getConstructor());

    assertEquals(season2, result2.getSeason());
    assertEquals(driver2, result2.getDriver());
    assertEquals(constructor2, result2.getConstructor());

    verify(seasonChampionRepository, times(2)).save(any(SeasonChampion.class));
  }

  @Test
  void whenSaveSeasonChampionWithComplexDriverData_thenMapCorrectly() {
    // Arrange
    String season = "2023";
    Driver complexDriver = new Driver();
    complexDriver.setDriverId("fernando_alonso");
    complexDriver.setGivenName("Fernando");
    complexDriver.setFamilyName("Alonso");
    complexDriver.setCode("ALO");
    complexDriver.setPermanentNumber("14");
    complexDriver.setNationality("Spanish");
    complexDriver.setDateOfBirth("1981-07-29");

    RaceWinner winnerWithComplexDriver = new RaceWinner();
    winnerWithComplexDriver.setDriver(complexDriver);
    winnerWithComplexDriver.setConstructor(testConstructor);

    SeasonChampion expectedChampion = new SeasonChampion();
    expectedChampion.setSeason(season);
    expectedChampion.setDriver(complexDriver);
    expectedChampion.setConstructor(testConstructor);

    when(seasonChampionRepository.save(any(SeasonChampion.class))).thenReturn(expectedChampion);

    // Act
    SeasonChampion result =
        seasonChampionService.saveSeasonChampion(season, winnerWithComplexDriver);

    // Assert
    assertEquals(season, result.getSeason());
    assertEquals("fernando_alonso", result.getDriver().getDriverId());
    assertEquals("Fernando", result.getDriver().getGivenName());
    assertEquals("Alonso", result.getDriver().getFamilyName());
    assertEquals("ALO", result.getDriver().getCode());
    assertEquals("14", result.getDriver().getPermanentNumber());
    assertEquals("Spanish", result.getDriver().getNationality());
    assertEquals("1981-07-29", result.getDriver().getDateOfBirth());

    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();
    assertEquals(complexDriver, capturedChampion.getDriver());
  }

  @Test
  void whenSaveSeasonChampionWithEmptyStringValues_thenHandleGracefully() {
    // Arrange
    String emptySeason = "";
    when(seasonChampionRepository.save(any(SeasonChampion.class))).thenReturn(testSeasonChampion);

    // Act
    SeasonChampion result = seasonChampionService.saveSeasonChampion(emptySeason, testRaceWinner);

    // Assert
    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();
    assertEquals("", capturedChampion.getSeason());
    assertEquals(testDriver, capturedChampion.getDriver());
    assertEquals(testConstructor, capturedChampion.getConstructor());
  }

  @Test
  void whenSaveSeasonChampionWithNullSeason_thenHandleGracefully() {
    // Arrange
    String nullSeason = null;
    when(seasonChampionRepository.save(any(SeasonChampion.class))).thenReturn(testSeasonChampion);

    // Act
    SeasonChampion result = seasonChampionService.saveSeasonChampion(nullSeason, testRaceWinner);

    // Assert
    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();
    assertNull(capturedChampion.getSeason());
    assertEquals(testDriver, capturedChampion.getDriver());
    assertEquals(testConstructor, capturedChampion.getConstructor());
  }

  @Test
  void whenMappingToSeasonChampion_thenAllFieldsSetCorrectly() {
    // Arrange
    String season = "2023";

    // Create a new season champion to verify mapping
    SeasonChampion mappedChampion = new SeasonChampion();
    mappedChampion.setSeason(season);
    mappedChampion.setDriver(testDriver);
    mappedChampion.setConstructor(testConstructor);

    when(seasonChampionRepository.save(any(SeasonChampion.class))).thenReturn(mappedChampion);

    // Act
    seasonChampionService.saveSeasonChampion(season, testRaceWinner);

    // Assert - Verify the mapping logic by checking what was passed to repository
    ArgumentCaptor<SeasonChampion> championCaptor = ArgumentCaptor.forClass(SeasonChampion.class);
    verify(seasonChampionRepository).save(championCaptor.capture());

    SeasonChampion capturedChampion = championCaptor.getValue();

    // Verify all fields are correctly mapped
    assertEquals(season, capturedChampion.getSeason());
    assertEquals(testRaceWinner.getDriver(), capturedChampion.getDriver());
    assertEquals(testRaceWinner.getConstructor(), capturedChampion.getConstructor());
  }
}
