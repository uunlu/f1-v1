package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonChampionService {
  private final SeasonChampionRepository seasonChampionRepository;

  public SeasonChampion saveSeasonChampion(final String season, final RaceWinner winner) {
    final SeasonChampion champion = mapToSeasonChampion(season, winner);
    return seasonChampionRepository.save(champion);
  }

  private SeasonChampion mapToSeasonChampion(final String season, final RaceWinner winner) {
    final SeasonChampion champion = new SeasonChampion();
    champion.setSeason(season);
    champion.setDriver(winner.getDriver());
    
    final Constructor constructor = winner.getConstructor();
    constructor.setSeason(season);  // Set the season for the constructor
    champion.setConstructor(constructor);
    
    return champion;
  }
} 