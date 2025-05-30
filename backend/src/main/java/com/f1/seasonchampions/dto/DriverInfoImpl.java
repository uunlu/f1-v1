package com.f1.seasonchampions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverInfoImpl implements DriverInfo {
  private String givenName;
  private String familyName;
  private String driverId;
}
