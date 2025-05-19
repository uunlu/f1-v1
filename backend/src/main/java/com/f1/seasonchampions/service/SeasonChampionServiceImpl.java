package com.f1.seasonchampions.service;

import com.f1.seasonchampions.dto.*;
import com.f1.seasonchampions.model.*;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Validated
public class SeasonChampionServiceImpl implements SeasonChampionService {
    private static final Logger logger = LoggerFactory.getLogger(SeasonChampionServiceImpl.class);

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    @Autowired
    public SeasonChampionServiceImpl(RestTemplateBuilder restTemplateBuilder, RetryTemplate retryTemplate) {
        this.restTemplate = restTemplateBuilder.build();
        this.retryTemplate = retryTemplate;
    }

    @Override
    public List<SeasonChampion> getSeasonChampions(SeasonRangeRequest request) {
        // Logical validation (in case startYear > endYear)
        if (request.getStartYear() > request.getEndYear()) {
            throw new IllegalArgumentException("Start year cannot be greater than end year");
        }

        List<SeasonChampion> champions = new ArrayList<>();

        for (int year = request.getStartYear(); year <= request.getEndYear(); year++) {
            final int currentYear = year;
            try {
                SeasonChampion champion = fetchChampionForYear(currentYear);
                if (champion != null) {
                    champions.add(champion);
                }
            } catch (Exception e) {
                logger.error("Failed to fetch champion for year {}: {}", currentYear, e.getMessage());
            }
        }

        return champions;
    }


    @Retryable(value = RestClientException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    private SeasonChampion fetchChampionForYear(int year) {
        String url = String.format("https://api.jolpi.ca/ergast/f1/%d/driverstandings/", year);
        ResponseEntity<DriverStandingsByYearResponse> response = restTemplate.getForEntity(url, DriverStandingsByYearResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            var driverStandingsByYearResponse = response.getBody();
            return parseSeasonChampion(driverStandingsByYearResponse);
        }
        return null;
    }

    private SeasonChampion parseSeasonChampion(DriverStandingsByYearResponse response) {
        // Existing code remains the same
        assert response != null;
        var table = response.getMrData().getStandingsTable();
        var season = table.getSeason();
        var winner = table.getStandingsLists().get(0).getDriverStandings().get(0);
        var driver = new Driver();
        driver.setDriverId(winner.getDriver().getDriverId());
        driver.setCode(winner.getDriver().getCode());
        driver.setPermanentNumber(winner.getDriver().getPermanentNumber());
        driver.setGivenName(winner.getDriver().getGivenName());
        driver.setFamilyName(winner.getDriver().getFamilyName());
        driver.setDateOfBirth(winner.getDriver().getDateOfBirth());
        driver.setNationality(winner.getDriver().getNationality());

        var constructor = new Constructor();
        constructor.setConstructorId(winner.getConstructors().get(0).getConstructorId());
        constructor.setName(winner.getConstructors().get(0).getName());
        constructor.setNationality(winner.getConstructors().get(0).getNationality());

        var champion = new SeasonChampion();
        champion.setSeason(season);
        champion.setDriver(driver);
        champion.setConstructor(constructor);
        return champion;
    }

    @Override
    public List<RaceWinner> getRaceWinners(int year) {
        try {
            return fetchRaceWinnersForYear(year);
        } catch (Exception e) {
            System.err.println("Failed to fetch race winners for year " + year + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Retryable(value = RestClientException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    private List<RaceWinner> fetchRaceWinnersForYear(int year) {
        String url = String.format("https://api.jolpi.ca/ergast/f1/%d/results/", year);
        ResponseEntity<ResultsByYearResponse> response = restTemplate.getForEntity(url, ResultsByYearResponse.class);
        ResultsByYearResponse result = response.getBody();

        if (result == null || result.getMrData() == null || result.getMrData().getRaceTable() == null) {
            return Collections.emptyList();
        }

        return result.getMrData().getRaceTable().getRaces().stream()
                .map(race -> {
                    var results = race.getResults();
                    if (results == null || results.isEmpty()) {
                        return null;
                    }

                    var winnerResult = results.get(0);  // Assuming index 0 is the winner
                    var raceWinner = new RaceWinner();
                    raceWinner.setRound(race.getRound());
                    raceWinner.setSeason(race.getSeason());
                    raceWinner.setTime(race.getTime());

                    var constructor = winnerResult.getConstructor();
                    if (constructor != null) {
                        var raceConstructor = new Constructor();
                        raceConstructor.setConstructorId(constructor.getConstructorId());
                        raceConstructor.setName(constructor.getName());
                        raceConstructor.setNationality(constructor.getNationality());
                        raceWinner.setConstructor(raceConstructor);
                    }

                    return raceWinner;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}