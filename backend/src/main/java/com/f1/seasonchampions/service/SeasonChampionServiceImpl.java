package com.f1.seasonchampions.service;

import com.f1.seasonchampions.dto.*;
import com.f1.seasonchampions.model.RaceWinner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.SeasonChampion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SeasonChampionServiceImpl implements SeasonChampionService {

    private final RestTemplate restTemplate;

    @Autowired
    public SeasonChampionServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public List<SeasonChampion> getSeasonChampions(int startYear, int endYear) {
        List<SeasonChampion> champions = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            String url = String.format("https://api.jolpi.ca/ergast/f1/%d/driverstandings/", year);
            ResponseEntity<DriverStandingsByYearResponse> response = restTemplate.getForEntity(url, DriverStandingsByYearResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                var result = response.getBody();
                assert result != null;
                var table = result.getMrData().getStandingsTable();
                var season = table.getSeason();
                // TODO: replace get(0) with a stream map perphaps position == 1
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
                System.out.println(champion);
                champions.add(champion);
            }

        }

        return champions;
    }

    @Override
    public List<RaceWinner> getRaceWinners(int year) {
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

                    // Optionally add more info like race name or winning driver here

                    return raceWinner;
                })
                .filter(Objects::nonNull)
                .toList();
    }

} 