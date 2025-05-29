package com.f1.seasonchampions.dto;

public interface RaceWinnerListItem {
  String getSeasonName();

  boolean isChampion();

  DriverInfo getDriver();

  String getSeasonDriverId();

  String getRound();
}
