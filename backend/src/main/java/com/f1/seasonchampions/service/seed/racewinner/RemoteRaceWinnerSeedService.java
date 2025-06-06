package com.f1.seasonchampions.service.seed.racewinner;

import com.f1.seasonchampions.dto.Race;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.service.RateLimitedApiClientService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteRaceWinnerSeedService implements RaceWinnerSeedService {
  private static final int MAX_PAGES = 50;
  private static final int MAX_TOTAL = 1000;
  private static final int DEFAULT_LIMIT = 30;

  private final RateLimitedApiClientService rateLimitedApiClient;

  @Value("${f1.api.base-url:https://api.jolpi.ca/ergast/f1}")
  private String apiBaseUrl;

  @Override
  public List<RaceWinner> getRaceWinners(final int year) {
    log.info("Fetching race winners from remote API for year: {}", year);

    try {
      return this.fetchRaceWinnersForYear(year);
    } catch (final Exception e) {
      log.error("Failed to fetch race winners for year {}: {}", year, e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public RaceWinner saveRaceWinner(final RaceWinner winner) {
    return winner; // Remote service doesn't save anything
  }

  @Override
  public boolean hasCompleteDataForYear(final int year) {
    return false; // Remote service always attempts to fetch fresh data
  }

  private List<RaceWinner> fetchRaceWinnersForYear(final int year) {
    int offset = 0;
    final int limit = DEFAULT_LIMIT;
    int total = Integer.MAX_VALUE;
    final List<RaceWinner> allWinners = new ArrayList<>();
    int pageCount = 0;

    log.info("Starting pagination for year {} with limit {} per page", year, limit);

    while (offset < total && pageCount < MAX_PAGES) {
      final String url =
          String.format(
              "%s/%d/results.json?limit=%d&offset=%d", this.apiBaseUrl, year, limit, offset);
      log.debug("Fetching page {} from URL: {}", pageCount + 1, url);

      try {
        final ResponseEntity<ResultsByYearResponse> response =
            this.rateLimitedApiClient.executeRateLimitedRequest(url, ResultsByYearResponse.class);

        final ResultsByYearResponse result = response.getBody();
        if (result == null
            || result.getMrData() == null
            || result.getMrData().getRaceTable() == null) {
          log.warn("Invalid or empty response at offset {} for year {}", offset, year);
          break;
        }

        if (total == Integer.MAX_VALUE) {
          try {
            total = Math.min(Integer.parseInt(result.getMrData().getTotal()), MAX_TOTAL);
            log.info("Total races available for year {}: {}", year, total);
          } catch (final NumberFormatException e) {
            log.error(
                "Could not parse total value: {} for year {}", result.getMrData().getTotal(), year);
            break;
          }
        }

        final List<Race> races = result.getMrData().getRaceTable().getRaces();
        log.debug("Received {} races in page {} for year {}", races.size(), pageCount + 1, year);

        final List<RaceWinner> winners =
            races.stream().map(this::mapToRaceWinner).flatMap(List::stream).toList();

        allWinners.addAll(winners);
        offset += limit;
        pageCount++;

        log.debug(
            "Page {} completed for year {}. Total winners so far: {}, Next offset: {}",
            pageCount,
            year,
            allWinners.size(),
            offset);

        // Check if we've retrieved all available data
        if (races.size() < limit) {
          log.info(
              "Received fewer races than limit ({} < {}), pagination complete for year {}",
              races.size(),
              limit,
              year);
          break;
        }

      } catch (final RestClientException e) {
        log.error("Request failed at offset {} for year {}: {}", offset, year, e.getMessage());
        break;
      }
    }

    log.info(
        "Pagination completed for year {}. Total winners fetched: {} from {} pages",
        year,
        allWinners.size(),
        pageCount);
    return allWinners;
  }

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
