package com.f1.seasonchampions.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SeasonChampionListItemImpl implements SeasonChampionListItem {
  private String season;
  private String driver;
  private String constructor;
  private boolean completed;

  // Constructor for backward compatibility (without isCompleted)
  public SeasonChampionListItemImpl(
      final String season, final String driver, final String constructor) {
    this.season = season;
    this.driver = driver;
    this.constructor = constructor;
    this.completed = false; // Will be set by service
  }

  @Override
  public boolean isCompleted() {
    return this.completed;
  }
}
