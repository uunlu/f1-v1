package com.f1.seasonchampions.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "constructors")
public class Constructor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "constructor_id")
  private String constructorId;

  @Column(name = "name")
  private String name;

  @Column(name = "nationality")
  private String nationality;

  @Column(name = "season")
  private String season;

  public Constructor(
      final String constructorId,
      final String name,
      final String nationality,
      final String season) {
    this.constructorId = constructorId;
    this.name = name;
    this.nationality = nationality;
    this.season = season;
  }
}
