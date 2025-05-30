package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.dto.Race;
import com.f1.seasonchampions.dto.Result;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@Slf4j
public class F1DataService {

  private final RestClient restClient;
  private final RaceWinnerRepository raceWinnerRepository;

  @Value("https://api.jolpi.ca/ergast/f1")
  private String baseUrl;

  public F1DataService(final RestClient restClient, final RaceWinnerRepository raceWinnerRepository) {
    this.restClient = restClient;
    this.raceWinnerRepository = raceWinnerRepository;
  }

  public boolean fetchAndStoreNewRaceWinners(final int year, final LocalDate afterDate) {
    log.info("Fetching new race winners for year {}", year);

    try {
      final var response =
          this.restClient
              .get()
              .uri(this.baseUrl + "/v1/races/" + year + "/winners")
              .retrieve()
              .body(ResultsByYearResponse.class);

      if (response == null
          || response.getMrData() == null
          || response.getMrData().getRaceTable() == null) {
        log.info("No race winners found for year {}", year);
        return false;
      }

      final var races = response.getMrData().getRaceTable().getRaces();
      boolean hasNewData = false;
      for (var race : races) {
        final LocalDate raceDate = LocalDate.parse(race.getDate());
        if (raceDate.isAfter(afterDate)) {
          log.info("New race data found: {} {}", race.getRaceName(), race.getDate());
          this.saveRaceResults(race);
          hasNewData = true;
        }
      }

      return hasNewData;
    } catch (Exception e) {
      log.error("Error while fetching new race data: {}", e.getMessage(), e);
      return false;
    }
  }

  private void saveRaceResults(final Race race) {
    if (race.getResults() == null) {
      log.info("No results found for race {} to save into the database.", race.getRaceName());
      return;
    }

    race.getResults()
        .forEach(
            result -> {
              final var raceResult = this.mapToRaceResult(race, result);
              this.raceWinnerRepository.save(raceResult);
            });
  }

  private RaceWinner mapToRaceResult(final Race race, final Result result) {
    final var raceWinner = new RaceWinner();
    raceWinner.setSeason(race.getSeason());
    raceWinner.setTime(race.getTime());

    final var driver = new Driver();
    driver.setDriverId(result.getDriver().getDriverId());
    driver.setCode(result.getDriver().getCode());
    driver.setNationality(result.getDriver().getNationality());
    driver.setDateOfBirth(result.getDriver().getDateOfBirth());
    driver.setPermanentNumber(result.getDriver().getPermanentNumber());
    driver.setFamilyName(result.getDriver().getFamilyName());
    driver.setGivenName(result.getDriver().getGivenName());
    raceWinner.setDriver(driver);

    final var constructor = new Constructor();
    constructor.setConstructorId(result.getConstructor().getConstructorId());
    constructor.setNationality(result.getConstructor().getNationality());
    constructor.setName(result.getConstructor().getName());
    raceWinner.setConstructor(constructor);

    return raceWinner;
  }
}
