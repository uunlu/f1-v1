package com.f1.seasonchampions.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RaceWinnerListItemImpl implements RaceWinnerListItem {
  private String seasonName;
  private String round;
  private boolean isChampion;
  private DriverInfo driver;
  private String seasonDriverId;
  private String seasonConstructorId;
  private String constructorName;
  private String time;
}
