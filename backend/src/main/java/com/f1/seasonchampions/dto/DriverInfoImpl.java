package com.f1.seasonchampions.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public  class DriverInfoImpl implements DriverInfo {
  private String givenName;
  private String familyName;
  private String driverId;
}
