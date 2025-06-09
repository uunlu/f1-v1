package com.f1.seasonchampions.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "season_champions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SeasonChampion {

  @Id private String season;

  @ManyToOne
  @JoinColumn(name = "driver_id")
  private Driver driver;

  @ManyToOne
  @JoinColumn(name = "constructor_id", referencedColumnName = "constructor_id")
  private Constructor constructor;
}
