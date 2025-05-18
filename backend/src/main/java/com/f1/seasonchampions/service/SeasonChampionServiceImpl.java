package com.f1.seasonchampions.service;

import com.f1.seasonchampions.dto.DriverStandingsByYearResponse;
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
            ResponseEntity<DriverStandingsByYearResponse> response = restTemplate.getForEntity(url, DriverStandingsByYearResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
            }

        }

        return champions;
    }


} 