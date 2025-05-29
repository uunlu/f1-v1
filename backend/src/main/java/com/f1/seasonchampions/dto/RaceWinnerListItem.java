package com.f1.seasonchampions.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public interface RaceWinnerListItem {
  String getSeasonName();
  boolean isChampion();
  DriverInfo getDriver();
  String getSeasonDriverId();
  String getRound();
}




