package com.f1.seasonchampions.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface SeasonChampionListItem {
  String getSeason();

  String getDriver();

  String getConstructor();
}
