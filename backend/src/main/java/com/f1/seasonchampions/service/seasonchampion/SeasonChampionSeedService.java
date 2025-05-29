package com.f1.seasonchampions.service.seasonchampion;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface SeasonChampionSeedService {
  @NotNull
  List<SeasonChampion> getSeasonChampions(@NotNull SeasonRangeRequest request);

  @NotNull
  List<SeasonChampion> getAllSeasonChampions();

  @NotNull
  SeasonChampion saveChampion(@NotNull SeasonChampion champion);
}
