package com.f1.seasonchampions.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "constructors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Constructor {
    @Id
    private String constructorId;
    private String name;
    private String nationality;
} 