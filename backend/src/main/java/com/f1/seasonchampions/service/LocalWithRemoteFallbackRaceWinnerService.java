package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.RaceWinner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class LocalWithRemoteFallbackRaceWinnerService implements RaceWinnerService {
    private final LocalRaceWinnerService localService;
    private final RemoteRaceWinnerService remoteService;

    @Override
    @Transactional
    public List<RaceWinner> getRaceWinners(int year) {
        if (localService.hasCompleteDataForYear(year)) {
            log.info("Returning race winners from local database for year: {}", year);
            return localService.getRaceWinners(year);
        }

        log.info("Fetching race winners from remote API for year: {}", year);
        List<RaceWinner> remoteWinners = remoteService.getRaceWinners(year);

        for (RaceWinner winner : remoteWinners) {
            log.info("Saving race winners from remote API for year: {}", year);
            localService.saveRaceWinner(winner);
        }

        return remoteWinners;
    }

    @Override
    @Transactional
    public RaceWinner saveRaceWinner(RaceWinner winner) {
        return localService.saveRaceWinner(winner);
    }

    @Override
    public boolean hasCompleteDataForYear(int year) {
        return localService.hasCompleteDataForYear(year);
    }
} 