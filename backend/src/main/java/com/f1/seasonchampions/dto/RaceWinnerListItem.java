package com.f1.seasonchampions.dto;

public interface RaceWinnerListItem {
  String getSeasonName();

  String getRound();

  boolean isChampion();

  DriverInfo getDriver();

  String getSeasonDriverId();

  String getSeasonConstructorId(); // Newly added

  String getConstructorName(); // Newly added
}
