package com.f1.seasonchampions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "race_winners", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"season", "round"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceWinner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String season;
    private String round;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "constructor_id", referencedColumnName = "id")
    private Constructor constructor;

    private String time;
}
