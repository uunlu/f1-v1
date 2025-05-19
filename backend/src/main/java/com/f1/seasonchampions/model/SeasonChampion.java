package com.f1.seasonchampions.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonChampion {
    private String season;
    private Driver driver;
    private Constructor constructor;
} 