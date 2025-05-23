package com.f1.seasonchampions.model;

import jakarta.persistence.*;
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
public class SeasonChampion {
    @Id
    private String season;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Constructor constructor;
}
