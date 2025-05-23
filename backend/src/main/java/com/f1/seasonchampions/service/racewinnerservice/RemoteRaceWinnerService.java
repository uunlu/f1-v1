package com.f1.seasonchampions.service.racewinnerservice;

import com.f1.seasonchampions.dto.Race;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteRaceWinnerService implements RaceWinnerService {
  private static final int MAX_PAGES = 50;
  private static final int MAX_TOTAL = 1000;

  private final RestTemplate restTemplate;
  private RateLimiter rateLimiter;

  @Value("${f1.api.base-url:https://api.jolpi.ca/ergast/f1}")
  private String apiBaseUrl;

  @PostConstruct
  public void init() {
    RateLimiterConfig config = RateLimiterConfig.custom()
      .limitRefreshPeriod(Duration.ofSeconds(1))
      .limitForPeriod(1)
      .timeoutDuration(Duration.ofSeconds(5))
      .build();

    RateLimiterRegistry registry = RateLimiterRegistry.of(config);
    rateLimiter = registry.rateLimiter("raceWinnerApiRateLimiter");
  }

  @Override
  public List<RaceWinner> getRaceWinners(int year) {
    log.info("Fetching race winners from remote API for year: {}", year);

    Supplier<List<RaceWinner>> rateLimitedCall = RateLimiter
      .decorateSupplier(rateLimiter, () -> fetchRaceWinnersForYear(year));

    try {
      return rateLimitedCall.get();
    } catch (Exception e) {
      log.error("Failed to fetch race winners for year {}: {}", year, e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public RaceWinner saveRaceWinner(RaceWinner winner) {
    // Remote service doesn't save anything
    return winner;
  }

  @Override
  public boolean hasCompleteDataForYear(int year) {
    // Remote service always attempts to fetch fresh data
    return false;
  }



  @Retryable(value = RestClientException.class, maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2))
  private List<RaceWinner> fetchRaceWinnersForYear(int year) {
    int offset = 0;
    int limit = 100;
    int total = Integer.MAX_VALUE;
    List<RaceWinner> allWinners = new ArrayList<>();
    int pageCount = 0;

    while (offset < total && pageCount < MAX_PAGES) {
      String url = String.format("%s/%d/results.json?limit=%d&offset=%d", apiBaseUrl, year, limit, offset);
      log.debug("Fetching race results from URL: {}", url);

      ResponseEntity<ResultsByYearResponse> response;
      try {
        response = restTemplate.getForEntity(url, ResultsByYearResponse.class);
      } catch (RestClientException e) {
        log.error("Request failed at offset {}: {}", offset, e.getMessage());
        break;
      }

      ResultsByYearResponse result = response.getBody();
      if (result == null || result.getMrData() == null || result.getMrData().getRaceTable() == null) {
        log.warn("Invalid or empty response at offset {}", offset);
        break;
      }

      if (total == Integer.MAX_VALUE) {
        try {
          total = Math.min(Integer.parseInt(result.getMrData().getTotal()), MAX_TOTAL);
        } catch (NumberFormatException e) {
          log.error("Could not parse total value: {}", result.getMrData().getTotal());
          break;
        }
      }

      List<RaceWinner> winners = result.getMrData()
        .getRaceTable()
        .getRaces()
        .stream()
        .map(this::mapToRaceWinner)
        .filter(Objects::nonNull)
        .toList();

      allWinners.addAll(winners);

      offset += limit;
      pageCount++;
    }

    return allWinners;
  }

  private RaceWinner mapToRaceWinner(Race race) {
    var results = race.getResults();
    if (results == null || results.isEmpty()) {
      log.warn("No results found for race {}", race.getRound());
      return null;
    }

    var winnerResult = results.get(0); // Assuming first result is the winner
    RaceWinner raceWinner = new RaceWinner();
    raceWinner.setRound(race.getRound());
    raceWinner.setSeason(race.getSeason());
    raceWinner.setTime(race.getTime());

    var constructor = winnerResult.getConstructor();
    if (constructor != null) {
      raceWinner.setConstructor(new Constructor(
        constructor.getConstructorId(),
        constructor.getName(),
        constructor.getNationality()
      ));
    }

    var driver = winnerResult.getDriver();
    if (driver != null) {
      raceWinner.setDriver(new Driver(
        driver.getDriverId(),
        driver.getPermanentNumber(),
        driver.getCode(),
        driver.getGivenName(),
        driver.getFamilyName(),
        driver.getDateOfBirth(),
        driver.getNationality()
      ));
    }

    return raceWinner;
  }
}
