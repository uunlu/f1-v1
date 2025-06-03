package com.f1.seasonchampions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RaceWinnerListItemImpl implements RaceWinnerListItem {
  private String seasonName;
  private String round;
  private boolean champion;
  private DriverInfo driver;
  private String seasonDriverId;
  private String seasonConstructorId;
  private String constructorName;
  private String time;
}
