package com.f1.seasonchampions.service;

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
import java.util.List;

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
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                champions.add(parseChampionFromResponse(response.getBody(), year));
            }
        }

        return champions;
    }

    private SeasonChampion parseChampionFromResponse(String responseBody, int year) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode driverNode = rootNode.path("MRData")
                                          .path("StandingsTable")
                                          .path("StandingsLists")
                                          .get(0)
                                          .path("DriverStandings")
                                          .get(0)
                                          .path("Driver");

            JsonNode constructorNode = rootNode.path("MRData")
                                               .path("StandingsTable")
                                               .path("StandingsLists")
                                               .get(0)
                                               .path("DriverStandings")
                                               .get(0)
                                               .path("Constructors")
                                               .get(0);

            Driver driver = new Driver(
                driverNode.path("driverId").asText(),
                driverNode.path("permanentNumber").asText(),
                driverNode.path("code").asText(),
                driverNode.path("givenName").asText(),
                driverNode.path("familyName").asText(),
                driverNode.path("dateOfBirth").asText(),
                driverNode.path("nationality").asText()
            );

            Constructor constructor = new Constructor(
                constructorNode.path("constructorId").asText(),
                constructorNode.path("name").asText(),
                constructorNode.path("nationality").asText()
            );

            return new SeasonChampion(year, driver, constructor);

        } catch (Exception e) {
            throw new RuntimeException("Error parsing API response for year " + year, e);
        }
    }
} 