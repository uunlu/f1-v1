package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface SeasonChampionService {
  @NotNull
  List<SeasonChampion> getSeasonChampions(@NotNull SeasonRangeRequest request);

  @NotNull
  SeasonChampion saveChampion(@NotNull SeasonChampion champion);
}
