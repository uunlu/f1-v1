package com.f1.seasonchampions.service.racewinner;

import com.f1.seasonchampions.model.RaceWinner;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface RaceWinnerService {
  @NotNull
  List<RaceWinner> getRaceWinners(int year);

  @NotNull
  RaceWinner saveRaceWinner(@NotNull RaceWinner winner);

  boolean hasCompleteDataForYear(int year);
}
