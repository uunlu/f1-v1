package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.RaceWinner;
import java.util.List;

public interface RaceWinnerService {
    List<RaceWinner> getRaceWinners(int year);
    RaceWinner saveRaceWinner(RaceWinner winner);
    boolean hasCompleteDataForYear(int year);
} 