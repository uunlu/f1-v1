package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import java.util.List;

public interface SeasonChampionService {
  List<SeasonChampion> getSeasonChampions(SeasonRangeRequest request);

  SeasonChampion saveChampion(SeasonChampion champion);
}
