package com.f1.seasonchampions.service;

import com.f1.seasonchampions.dto.DriverStandingsByYearResponse;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.exception.InvalidInputException;
import com.f1.seasonchampions.model.*;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@Service
public class SeasonChampionService {
    private static final String API_BASE_URL = "https://api.jolpi.ca/ergast/f1";
    private final RestTemplate restTemplate;

    @Autowired
    public SeasonChampionService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public List<SeasonChampion> getSeasonChampions(SeasonRangeRequest request) {
        log.info("Fetching season champions from {} to {}", request.getStartYear(), request.getEndYear());

        if (request.getStartYear() > request.getEndYear()) {
            throw new InvalidInputException("Start year cannot be greater than end year");
        }

        // TODO: replace magic numbers with constants
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(1)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter rateLimiter = registry.rateLimiter("apiRateLimiter");

        List<SeasonChampion> champions = new ArrayList<>();

        for (int year = request.getStartYear(); year <= request.getEndYear(); year++) {
            final int currentYear = year;

            // Wrap the API call with rate limiter
            Supplier<SeasonChampion> rateLimitedCall = RateLimiter
                    .decorateSupplier(rateLimiter, () -> fetchChampionForYear(currentYear));

            try {
                SeasonChampion champion = rateLimitedCall.get();
                if (champion != null) {
                    champions.add(champion);
                    log.debug("Successfully fetched champion for year {}: {}", currentYear, champion);
                }
            } catch (Exception e) {
                log.error("Failed to fetch champion for year {}: {}", currentYear, e.getMessage());
            }
        }

        log.info("Retrieved {} champions", champions.size());
        return champions;
    }

    @Retryable(value = RestClientException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    private SeasonChampion fetchChampionForYear(int year) {
        String url = String.format("%s/%d/driverstandings/", API_BASE_URL, year);
        log.debug("Fetching champion data from URL: {}", url);

        ResponseEntity<DriverStandingsByYearResponse> response = restTemplate.getForEntity(url, DriverStandingsByYearResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return parseSeasonChampion(response.getBody());
        }

        log.warn("No champion data found for year {}", year);
        return null;
    }

    private SeasonChampion parseSeasonChampion(DriverStandingsByYearResponse response) {
        if (response == null || response.getMrData() == null || response.getMrData().getStandingsTable() == null) {
            log.warn("Invalid response format for champion data");
            return null;
        }

        var table = response.getMrData().getStandingsTable();
        var season = table.getSeason();
        var winner = table.getStandingsLists().get(0).getDriverStandings().get(0);
        
        var driver = new Driver(
            winner.getDriver().getDriverId(),
            winner.getDriver().getPermanentNumber(),
            winner.getDriver().getCode(),
            winner.getDriver().getGivenName(),
            winner.getDriver().getFamilyName(),
            winner.getDriver().getDateOfBirth(),
            winner.getDriver().getNationality()
        );

        var constructor = new Constructor(
            winner.getConstructors().get(0).getConstructorId(),
            winner.getConstructors().get(0).getName(),
            winner.getConstructors().get(0).getNationality()
        );

        return new SeasonChampion(season, driver, constructor);
    }

    public List<RaceWinner> getRaceWinners(int year) {
        log.info("Fetching race winners for year {}", year);
        try {
            return fetchRaceWinnersForYear(year);
        } catch (Exception e) {
            log.error("Failed to fetch race winners for year {}: {}", year, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Retryable(value = RestClientException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    private List<RaceWinner> fetchRaceWinnersForYear(int year) {
        String url = String.format("%s/%d/results/", API_BASE_URL, year);
        log.debug("Fetching race results from URL: {}", url);
        
        ResponseEntity<ResultsByYearResponse> response = restTemplate.getForEntity(url, ResultsByYearResponse.class);
        ResultsByYearResponse result = response.getBody();

        if (result == null || result.getMrData() == null || result.getMrData().getRaceTable() == null) {
            log.warn("Invalid response format for race results");
            return Collections.emptyList();
        }

        return result.
                getMrData()
                .getRaceTable()
                .getRaces()
                .stream()
                .map(race -> {
                    var results = race.getResults();
                    if (results == null || results.isEmpty()) {
                        log.warn("No results found for race {}", race.getRound());
                        return null;
                    }

                    var winnerResult = results.get(0);
                    var raceWinner = new RaceWinner();
                    raceWinner.setRound(race.getRound());
                    raceWinner.setSeason(race.getSeason());
                    raceWinner.setTime(race.getTime());

                    var constructor = winnerResult.getConstructor();
                    if (constructor != null) {
                        var raceConstructor = new Constructor(
                            constructor.getConstructorId(),
                            constructor.getName(),
                            constructor.getNationality()
                        );
                        raceWinner.setConstructor(raceConstructor);
                    }

                    return raceWinner;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}