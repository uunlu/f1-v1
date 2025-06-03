package com.f1.seasonchampions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeasonChampionListItemImpl implements SeasonChampionListItem {
  private String season;
  private String driver;
  private String constructor;
}
