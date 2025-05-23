package com.f1.seasonchampions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "constructors")
@Data
@NoArgsConstructor
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

    public Constructor(String constructorId, String name, String nationality) {
        this.constructorId = constructorId;
        this.name = name;
        this.nationality = nationality;
    }
}
