package com.f1.seasonchampions.service;

import com.f1.seasonchampions.dto.Driver;
import com.f1.seasonchampions.dto.DriverStandingsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.SeasonChampion;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class F1ApiClient {

  private final RestTemplate restTemplate;

  @Value("https://api.jolpi.ca/ergast/")
  private String baseUrl;

  @Nullable
  public SeasonChampion fetchChampionForSeason(final int year) {
    log.info("Fetching F1 champion data for year: {}", year);

    try {
      final String url = String.format("%s/f1/%d/driverstandings/", this.baseUrl, year);

      final DriverStandingsByYearResponse response =
          this.restTemplate.getForObject(url, DriverStandingsByYearResponse.class);

      if (response != null && response.getMrData() != null) {
        return this.mapToSeasonChampion(response, String.valueOf(year));
      } else {
        log.warn("No champion data found for year: {}", year);
        return null;
      }
    } catch (Exception e) {
      log.error("Error fetching F1 champion for year {}: {}", year, e.getMessage());
      throw new F1ApiException("Failed to fetch champion data for year: " + year, e);
    }
  }

  @NotNull
  private SeasonChampion mapToSeasonChampion(
      @NotNull final DriverStandingsByYearResponse response, @NotNull final String year) {
    final Driver driverDto =
        response
            .getMrData()
            .getStandingsTable()
            .getStandingsLists()
            .getFirst()
            .getDriverStandings()
            .getFirst()
            .getDriver();

    final var constructorDto =
        response
            .getMrData()
            .getStandingsTable()
            .getStandingsLists()
            .getFirst()
            .getDriverStandings()
            .getFirst()
            .getConstructors()
            .getFirst();

    final com.f1.seasonchampions.model.Driver driver = new com.f1.seasonchampions.model.Driver();
    driver.setDriverId(driverDto.getDriverId());
    driver.setCode(driverDto.getCode());
    driver.setNationality(driverDto.getNationality());
    driver.setDateOfBirth(driverDto.getDateOfBirth());
    driver.setPermanentNumber(driverDto.getPermanentNumber());
    driver.setFamilyName(driverDto.getFamilyName());
    driver.setGivenName(driverDto.getGivenName());

    final var constructor = new Constructor();
    constructor.setConstructorId(constructorDto.getConstructorId());
    constructor.setNationality(constructorDto.getNationality());
    constructor.setName(constructorDto.getName());

    return SeasonChampion.builder()
        .season(String.valueOf(year))
        .driver(driver)
        .constructor(constructor)
        .build();
  }

  // Inner classes for API response structure
  @lombok.Data
  private static final class F1ApiResponse {
    private DriverData driver;
    private TeamData team;
    private int points;
    private int wins;
  }

  @lombok.Data
  private static final class DriverData {
    private String driverId;
    private String firstName;
    private String lastName;
    private String nationality;
    private String code;
  }

  @lombok.Data
  private static final class TeamData {
    private String teamId;
    private String name;
  }

  // Custom exception class for API errors
  public static class F1ApiException extends RuntimeException {
    public F1ApiException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}
