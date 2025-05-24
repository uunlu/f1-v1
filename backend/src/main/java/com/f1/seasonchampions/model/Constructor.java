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

@Entity
@Table(name = "constructors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Constructor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "constructor_id", nullable = false)
  private String constructorId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "nationality")
  private String nationality;

  public Constructor(final String constructorId, final String name, final String nationality) {
    this.constructorId = constructorId;
    this.name = name;
    this.nationality = nationality;
  }
}
