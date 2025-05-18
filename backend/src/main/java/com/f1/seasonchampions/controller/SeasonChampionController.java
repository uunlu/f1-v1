package com.f1.seasonchampions.controller;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.service.SeasonChampionService;
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
            @RequestParam(defaultValue = "2005") int startYear,
            @RequestParam(defaultValue = "2024") int endYear) {
        return seasonChampionService.getSeasonChampions(startYear, endYear);
    }
} 