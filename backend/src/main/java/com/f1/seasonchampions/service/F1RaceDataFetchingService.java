package com.f1.seasonchampions.service;

import com.f1.seasonchampions.dto.generated.Race;
import com.f1.seasonchampions.dto.generated.ResultsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Shared service for fetching F1 race data from external APIs. Centralizes the race fetching logic
 * that was previously duplicated across multiple services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class F1RaceDataFetchingService {

  private final RateLimitedApiClientService rateLimitedApiClient;

  @Value("${f1.api.base-url:https://api.jolpi.ca/ergast/f1}")
  private String apiBaseUrl;

  private static final int MAX_CONSECUTIVE_EMPTY_ROUNDS = 3;
  private static final int MAX_ROUNDS_PER_SEASON = 30;

  /**
   * Fetches race winners for a specific year using round-by-round API calls.
   *
   * @param year The year to fetch race winners for
   * @return List of race winners for the year
   */
  public List<RaceWinner> fetchRaceWinnersForYear(final int year) {
    final List<RaceWinner> allWinners = new ArrayList<>();
    int round = 1;
    int consecutiveEmptyRounds = 0;

    log.info("Starting round-by-round fetching for year {}", year);

    while (consecutiveEmptyRounds < MAX_CONSECUTIVE_EMPTY_ROUNDS
        && round <= MAX_ROUNDS_PER_SEASON) {
      final String url = String.format("%s/%d/%d/results.json", this.apiBaseUrl, year, round);

      log.debug("Fetching round {} from URL: {}", round, url);

      try {
        final ResponseEntity<ResultsByYearResponse> response =
            this.rateLimitedApiClient.executeRateLimitedRequest(url, ResultsByYearResponse.class);

        final ResultsByYearResponse result = response.getBody();
        if (result == null
            || result.getMrData() == null
            || result.getMrData().getRaceTable() == null
            || result.getMrData().getRaceTable().getRaces() == null
            || result.getMrData().getRaceTable().getRaces().isEmpty()) {
          log.debug("No race data found for round {} in year {}", round, year);
          consecutiveEmptyRounds++;
        } else {
          // Reset counter when we find data
          consecutiveEmptyRounds = 0;

          final List<Race> races = result.getMrData().getRaceTable().getRaces();
          log.debug("Received {} races for round {} in year {}", races.size(), round, year);

          final List<RaceWinner> winners =
              races.stream().map(this::mapToRaceWinner).flatMap(List::stream).toList();

          allWinners.addAll(winners);

          log.debug(
              "Round {} completed for year {}. Winners so far: {}", round, year, allWinners.size());
        }

      } catch (final RestClientException e) {
        log.debug(
            "Request failed for round {} in year {}: {} - trying next round",
            round,
            year,
            e.getMessage());
        consecutiveEmptyRounds++;
      }

      round++; // Always move to next round
    }

    log.info(
        "Round-by-round fetching completed for year {}. Total winners fetched: {} from {} rounds checked",
        year,
        allWinners.size(),
        round - 1);
    return allWinners;
  }

  /**
   * Maps a Race DTO to a list of RaceWinner entities. Extracts the winner (position 1) from the
   * race results.
   *
   * @param race The race DTO from the API response
   * @return List containing the race winner, or empty list if no winner found
   */
  @NotNull
  private List<RaceWinner> mapToRaceWinner(final Race race) {
    if (race == null) {
      log.warn("Race object is null");
      return Collections.emptyList();
    }

    final var results = race.getResults();
    if (results == null || results.isEmpty()) {
      log.warn("No results found for race round {}", race.getRound());
      return Collections.emptyList();
    }

    final var winnerResult =
        results.stream()
            .filter(result -> result != null && "1".equals(result.getPosition()))
            .findFirst();

    if (winnerResult.isEmpty()) {
      log.warn("No winner found in race round {} for season {}", race.getRound(), race.getSeason());
      return Collections.emptyList();
    }

    final var winner = winnerResult.get();
    final var constructor = winner.getConstructor();
    final var driver = winner.getDriver();
    final var time = winner.getTime();

    if (driver == null || constructor == null) {
      log.warn(
          "Skipping incomplete winner data - Round: {}, Season: {}, Has Driver: {}, Has Constructor: {}",
          race.getRound(),
          race.getSeason(),
          driver != null,
          constructor != null);
      return Collections.emptyList();
    }

    final RaceWinner raceWinner = new RaceWinner();
    raceWinner.setRound(race.getRound());
    raceWinner.setSeason(race.getSeason());
    raceWinner.setTime(time.getTime());
    raceWinner.setConstructor(
        new Constructor(
            constructor.getConstructorId(), constructor.getName(), constructor.getNationality()));
    raceWinner.setDriver(
        new Driver(
            driver.getDriverId(),
            driver.getPermanentNumber(),
            driver.getCode(),
            driver.getGivenName(),
            driver.getFamilyName(),
            driver.getDateOfBirth(),
            driver.getNationality()));

    log.info(
        "Winner mapped successfully - Round: {}, Season: {}, Driver: {} {}, Constructor: {}",
        raceWinner.getRound(),
        raceWinner.getSeason(),
        driver.getGivenName(),
        driver.getFamilyName(),
        constructor.getName());

    return Collections.singletonList(raceWinner);
  }
}
