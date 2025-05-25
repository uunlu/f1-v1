package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.*;
import com.f1.seasonchampions.model.SeasonChampion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class F1ApiClientTest {

  @Mock private RestTemplate restTemplate;
  @InjectMocks private F1ApiClient f1ApiClient;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(f1ApiClient, "baseUrl", "https://api.jolpi.ca/ergast");
  }

  @Test
  void whenFetchChampionSuccess_thenReturnSeasonChampion() {
    // Arrange
    var dtoDriver = new Driver();
    dtoDriver.setDriverId("max_verstappen");
    dtoDriver.setCode("VER");
    dtoDriver.setNationality("Dutch");
    dtoDriver.setDateOfBirth("1997-09-30");
    dtoDriver.setPermanentNumber("33");
    dtoDriver.setGivenName("Max");
    dtoDriver.setFamilyName("Verstappen");

    var constructorDto = new Constructor();
    constructorDto.setConstructorId("red_bull");
    constructorDto.setName("Red Bull Racing");
    constructorDto.setNationality("Austrian");

    var response = new DriverStandingsByYearResponse();
    var mrData = new DriverStandingsByYearResponseMRData();
    var standingsTable = new DriverStandingsByYearResponseMRDataStandingsTable();
    standingsTable.setSeason("2023");

    var standingsListInner =
        new DriverStandingsByYearResponseMRDataStandingsTableStandingsListsInner();
    var driverStanding =
        new DriverStandingsByYearResponseMRDataStandingsTableStandingsListsInnerDriverStandingsInner();

    driverStanding.setDriver(dtoDriver);
    driverStanding.setConstructors(List.of(constructorDto));

    standingsListInner.setDriverStandings(new ArrayList<>());
    standingsListInner.getDriverStandings().add(driverStanding);

    standingsTable.setStandingsLists(new ArrayList<>());
    standingsTable.getStandingsLists().add(standingsListInner);

    mrData.setStandingsTable(standingsTable);
    response.setMrData(mrData);

    when(restTemplate.getForObject(
            "https://api.jolpi.ca/ergast/f1/2023/driverstandings/",
            DriverStandingsByYearResponse.class))
        .thenReturn(response);

    // Act
    SeasonChampion result = f1ApiClient.fetchChampionForSeason(2023);

    // Assert
    assertNotNull(result);
    assertEquals("2023", result.getSeason());
    assertEquals("max_verstappen", result.getDriver().getDriverId());
    assertEquals("red_bull", result.getConstructor().getConstructorId());
  }

  @Test
  void whenFetchChampionResponseIsNull_thenReturnNull() {
    when(restTemplate.getForObject(anyString(), eq(DriverStandingsByYearResponse.class)))
        .thenReturn(null);

    SeasonChampion result = f1ApiClient.fetchChampionForSeason(2023);

    assertNull(result);
  }

  @Test
  void whenRestCallFails_thenThrowF1ApiException() {
    when(restTemplate.getForObject(anyString(), eq(DriverStandingsByYearResponse.class)))
        .thenThrow(new RuntimeException("Timeout"));

    F1ApiClient.F1ApiException ex =
        assertThrows(
            F1ApiClient.F1ApiException.class, () -> f1ApiClient.fetchChampionForSeason(2023));

    assertTrue(ex.getMessage().contains("Failed to fetch champion data"));
  }
}
