package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalRaceWinnerService implements RaceWinnerService {
    private final RaceWinnerRepository raceWinnerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RaceWinner> getRaceWinners(int year) {
        log.info("Fetching race winners from local database for year: {}", year);
        return raceWinnerRepository.findBySeasonOrderByRound(String.valueOf(year));
    }

    @Override
    @Transactional
    public RaceWinner saveRaceWinner(RaceWinner winner) {
        log.debug("Saving race winner to database: {}", winner);
        return raceWinnerRepository.save(winner);
    }

    @Override
    public boolean hasCompleteDataForYear(int year) {
        List<RaceWinner> existingWinners = getRaceWinners(year);
        // TODO: Implement proper validation based on expected number of races per season
        // For now, assume if we have any data, it's complete
        return !existingWinners.isEmpty();
    }
} 