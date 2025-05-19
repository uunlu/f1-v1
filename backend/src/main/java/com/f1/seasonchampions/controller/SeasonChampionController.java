package com.f1.seasonchampions.controller;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.service.SeasonChampionService;
import com.f1.seasonchampions.validation.CurrentYearConstraint;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeasonChampionController {

    private final SeasonChampionService seasonChampionService;

    @Autowired
    public SeasonChampionController(SeasonChampionService seasonChampionService) {
        this.seasonChampionService = seasonChampionService;
    }

    @GetMapping("/api/season-champions")
    public List<SeasonChampion> getSeasonChampions(
            @RequestParam(defaultValue = "2005")
            @Min(value = 1950, message = "Start year must be 1950 or later") int startYear,

            @RequestParam(defaultValue = "2024")
            @CurrentYearConstraint int endYear) {

        var request = new SeasonRangeRequest(startYear, endYear);
        return seasonChampionService.getSeasonChampions(request);
    }

    @GetMapping("/api/results")
    public List<RaceWinner> getRaceResults(
            @RequestParam(defaultValue = "2005") int year) {
        return seasonChampionService.getRaceWinners(year);
    }
} 