package com.f1.seasonchampions.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public interface SeasonChampionListItem {
  String getSeason();

  String getDriver();

  String getConstructor();

  /**
   * Indicates whether this F1 season has concluded. A season is considered completed if: - It's a
   * past year, or - It's the current year but we're past the typical season end month (December),
   * or - All races for the season have been completed
   *
   * @return true if the season has concluded, false if it's ongoing
   */
  boolean isCompleted();
}
