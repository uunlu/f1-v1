package com.f1.seasonchampions.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RaceWinnerListItemImpl implements RaceWinnerListItem {
  private String seasonName;
  private boolean champion;
  private  DriverInfo driver;
  private  String seasonDriverId;
}
