package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.SeasonChampion;
import java.util.List;

public interface SeasonChampionService {
    List<SeasonChampion> getSeasonChampions(int startYear, int endYear);
} 