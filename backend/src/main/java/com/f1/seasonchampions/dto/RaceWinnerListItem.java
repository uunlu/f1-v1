package com.f1.seasonchampions.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public interface RaceWinnerListItem {
  String getSeasonName();

  String getRound();

  boolean isChampion();

  DriverInfo getDriver();

  String getSeasonDriverId();

  String getSeasonConstructorId(); // Newly added

  String getConstructorName();

  String getTime();
}
