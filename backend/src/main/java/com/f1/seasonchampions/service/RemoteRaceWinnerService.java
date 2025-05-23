package com.f1.seasonchampions.service;

import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteRaceWinnerService implements RaceWinnerService {
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
        String url = String.format("%s/%d/results/", apiBaseUrl, year);
        log.debug("Fetching race results from URL: {}", url);
        
        ResponseEntity<ResultsByYearResponse> response = restTemplate.getForEntity(url, ResultsByYearResponse.class);
        ResultsByYearResponse result = response.getBody();

        if (result == null || result.getMrData() == null || result.getMrData().getRaceTable() == null) {
            log.warn("Invalid response format for race results");
            return Collections.emptyList();
        }

        return result.getMrData()
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