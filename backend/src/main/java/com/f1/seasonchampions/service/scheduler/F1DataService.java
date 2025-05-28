package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.dto.Race;
import com.f1.seasonchampions.dto.Result;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

public class F1DataService {

  private static final Logger logger = LoggerFactory.getLogger(F1DataService.class);

  private final RestClient restClient;
  private final RaceWinnerRepository raceWinnerRepository;
  @Value("https://api.jolpi.ca/ergast/f1")
  private String baseUrl;


  public F1DataService(RestClient restClient, RaceWinnerRepository raceWinnerRepository) {
    this.restClient = restClient;
    this.raceWinnerRepository = raceWinnerRepository;
  }

  public boolean fetchAndStoreNewRaceWinners(int year, LocalDate afterDate) {
    logger.info("Fetching new race winners for year {}", year);

    try {
      var response = restClient.get()
        .uri(baseUrl + "/v1/races/" + year + "/winners")
        .retrieve()
        .body(ResultsByYearResponse.class);

      if (response == null ||
        response.getMrData() == null ||
        response.getMrData().getRaceTable() == null) {
        logger.info("No race winners found for year {}", year);
        return false;
      }

      var races = response.getMrData().getRaceTable().getRaces();
      boolean hasNewData = false;
      for (var race : races) {
        LocalDate raceDate = LocalDate.parse(race.getDate());
        if (raceDate.isAfter(afterDate)) {
          logger.info("New race data found: {} {}", race.getRaceName(), race.getDate());
          saveRaceResults(race);
          hasNewData = true;
        }
      }

      return hasNewData;
    } catch (Exception e) {
      logger.error("Error while fetching new race data: {}", e.getMessage(), e);
      return false;
    }
  }

  private void saveRaceResults(Race race) {
    if (race.getResults() == null) {
      logger.info("No results found for race {} to save into the database.", race.getRaceName());
      return;
    }

    race.getResults().forEach(result -> {
      var raceResult = mapToRaceResult(race, result);
      raceWinnerRepository.save(raceResult);
    });
  }

  private RaceWinner mapToRaceResult(Race race, Result result) {
    var raceWinner = new RaceWinner();
    raceWinner.setSeason(race.getSeason());
    raceWinner.setTime(race.getTime());

    var driver = new Driver();
    driver.setDriverId(result.getDriver().getDriverId());
    driver.setCode(result.getDriver().getCode());
    driver.setNationality(result.getDriver().getNationality());
    driver.setDateOfBirth(result.getDriver().getDateOfBirth());
    driver.setPermanentNumber(result.getDriver().getPermanentNumber());
    driver.setFamilyName(result.getDriver().getFamilyName());
    driver.setGivenName(result.getDriver().getGivenName());
    raceWinner.setDriver(driver);

    var constructor = new Constructor();
    constructor.setConstructorId(result.getConstructor().getConstructorId());
    constructor.setNationality(result.getConstructor().getNationality());
    constructor.setName(result.getConstructor().getName());
    raceWinner.setConstructor(constructor);

    return raceWinner;
  }
}
