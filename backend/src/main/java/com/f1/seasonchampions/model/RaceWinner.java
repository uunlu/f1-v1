package com.f1.seasonchampions.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceWinner {
    private String season;
    private String round;
    private Driver driver;
    private Constructor constructor;
    private String time;
}
