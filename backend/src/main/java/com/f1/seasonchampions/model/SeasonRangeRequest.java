package com.f1.seasonchampions.model;

import com.f1.seasonchampions.validation.CurrentYearConstraint;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonRangeRequest {
  @Min(value = 1950, message = "Start year must be 1950 or later")
  private int startYear;

  @CurrentYearConstraint private int endYear;
}
