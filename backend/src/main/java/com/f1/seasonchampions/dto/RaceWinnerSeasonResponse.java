package com.f1.seasonchampions.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Response wrapper for race winners with season metadata. */
@Data
@AllArgsConstructor
public class RaceWinnerSeasonResponse {
  private String season;
  private boolean isSeasonConcluded;
  private boolean hasChampion;
  private int totalRaces;
  private List<RaceWinnerListItem> raceWinners;
}
