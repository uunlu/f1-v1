package com.f1.seasonchampions.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SeasonChampionListItemImpl implements SeasonChampionListItem {
  private String season;
  private String driver;
  private String constructor;
}
