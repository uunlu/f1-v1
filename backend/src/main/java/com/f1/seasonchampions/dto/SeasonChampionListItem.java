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
}
