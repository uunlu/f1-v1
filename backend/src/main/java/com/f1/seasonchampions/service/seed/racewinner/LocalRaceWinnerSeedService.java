package com.f1.seasonchampions.service.seed.racewinner;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalRaceWinnerSeedService implements RaceWinnerSeedService {
  private final RaceWinnerRepository raceWinnerRepository;
  private final DriverRepository driverRepository;
  private final ConstructorRepository constructorRepository;

  @Override
  @Transactional(readOnly = true)
  public List<RaceWinner> getRaceWinners(final int year) {
    log.info("Fetching race winners from local database for year: {}", year);
    final List<RaceWinner> winners =
        this.raceWinnerRepository.findBySeasonAndOptionalRound(String.valueOf(year), null);
    log.info("Fetched {} race winners", winners.size());
    for (RaceWinner winner : winners) {
      log.info(
          "Race: {}, Round: {}, Winner: {}",
          winner.getSeason(),
          winner.getRound(),
          winner.getDriver().getGivenName());
    }
    return winners;
  }

  @Override
  @Transactional
  public RaceWinner saveRaceWinner(final RaceWinner winner) {
    log.debug("Saving race winner to database: {}", winner);

    final List<RaceWinner> existingWinners =
        this.raceWinnerRepository.findBySeasonAndOptionalRound(
            winner.getSeason(), winner.getRound());

    if (!existingWinners.isEmpty()) {
      log.debug(
          "Race winner already exists for season {} round {}",
          winner.getSeason(),
          winner.getRound());
      return existingWinners.get(0);
    }

    if (winner.getDriver() != null) {
      final Optional<Driver> existingDriver =
          this.driverRepository.findById(winner.getDriver().getDriverId());

      if (existingDriver.isPresent()) {
        winner.setDriver(existingDriver.get());
      } else {
        winner.setDriver(this.driverRepository.save(winner.getDriver()));
      }
    }

    if (winner.getConstructor() != null) {
      final String constructorId = winner.getConstructor().getConstructorId();
      log.debug("Looking up constructor with ID: {}", constructorId);
      final Optional<Constructor> existingConstructor =
          this.constructorRepository.findByConstructorId(constructorId);

      if (existingConstructor.isPresent()) {
        log.debug("Found existing constructor: {}", existingConstructor.get().getName());
        winner.setConstructor(existingConstructor.get());
      } else {
        log.debug("Saving new constructor: {}", winner.getConstructor().getName());
        winner.setConstructor(this.constructorRepository.save(winner.getConstructor()));
      }
    }

    final RaceWinner savedWinner = this.raceWinnerRepository.save(winner);
    log.debug(
        "Saved race winner with constructor: {}",
        savedWinner.getConstructor() != null ? savedWinner.getConstructor().getName() : "null");
    return savedWinner;
  }

  @Override
  public boolean hasCompleteDataForYear(final int year) {
    final List<RaceWinner> existingWinners = this.getRaceWinners(year);
    // TODO: Implement proper validation based on expected number of races per season
    return !existingWinners.isEmpty();
  }
}
