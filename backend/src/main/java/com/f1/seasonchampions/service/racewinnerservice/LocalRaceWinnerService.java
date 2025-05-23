package com.f1.seasonchampions.service.racewinnerservice;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.ConstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalRaceWinnerService implements RaceWinnerService {
    private final RaceWinnerRepository raceWinnerRepository;
    private final DriverRepository driverRepository;
    private final ConstructorRepository constructorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RaceWinner> getRaceWinners(final int year) {
        log.info("Fetching race winners from local database for year: {}", year);
        return raceWinnerRepository.findBySeasonAndOptionalRound(String.valueOf(year), null);
    }

    @Override
    @Transactional
    public RaceWinner saveRaceWinner(RaceWinner winner) {
        log.debug("Saving race winner to database: {}", winner);

        // Check if race winner already exists
        List<RaceWinner> existingWinners = raceWinnerRepository.findBySeasonAndOptionalRound(
            winner.getSeason(), winner.getRound());

        if (!existingWinners.isEmpty()) {
            log.debug("Race winner already exists for season {} round {}",
                winner.getSeason(), winner.getRound());
            return existingWinners.get(0);
        }

        // Handle Driver entity
        if (winner.getDriver() != null) {
            Optional<com.f1.seasonchampions.model.Driver> existingDriver =
                driverRepository.findById(winner.getDriver().getDriverId());

            if (existingDriver.isPresent()) {
                winner.setDriver(existingDriver.get());
            } else {
                winner.setDriver(driverRepository.save(winner.getDriver()));
            }
        }

        // Handle Constructor entity
        if (winner.getConstructor() != null) {
            String constructorId = winner.getConstructor().getConstructorId();
            Optional<com.f1.seasonchampions.model.Constructor> existingConstructor =
                constructorRepository.findByConstructorId(constructorId);

            if (existingConstructor.isPresent()) {
                winner.setConstructor(existingConstructor.get());
            } else {
                winner.setConstructor(constructorRepository.save(winner.getConstructor()));
            }
        }

        return raceWinnerRepository.save(winner);
    }

    @Override
    public boolean hasCompleteDataForYear(int year) {
        List<RaceWinner> existingWinners = getRaceWinners(year);
        // TODO: Implement proper validation based on expected number of races per season
        return !existingWinners.isEmpty();
    }
}
