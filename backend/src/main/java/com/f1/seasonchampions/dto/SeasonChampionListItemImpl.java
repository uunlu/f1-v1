package com.f1.seasonchampions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeasonChampionListItemImpl implements SeasonChampionListItem {
  private String season;
  private String driver;
  private String constructor;
}
