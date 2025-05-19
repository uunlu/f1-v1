package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface SeasonChampionService {
    List<SeasonChampion> getSeasonChampions(@Valid SeasonRangeRequest request);
    List<RaceWinner> getRaceWinners(int year);

}