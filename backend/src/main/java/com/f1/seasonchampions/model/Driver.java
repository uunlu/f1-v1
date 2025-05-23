package com.f1.seasonchampions.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
  @Id private String driverId;
  private String permanentNumber;
  private String code;
  private String givenName;
  private String familyName;
  private String dateOfBirth;
  private String nationality;
}
