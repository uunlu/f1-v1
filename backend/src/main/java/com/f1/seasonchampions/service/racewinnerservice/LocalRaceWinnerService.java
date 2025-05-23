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
    return this.raceWinnerRepository.findBySeasonAndOptionalRound(String.valueOf(year), null);
  }

  @Override
  @Transactional
  public RaceWinner saveRaceWinner(final RaceWinner winner) {
    log.debug("Saving race winner to database: {}", winner);

    final List<RaceWinner> existingWinners = this.raceWinnerRepository.findBySeasonAndOptionalRound(
      winner.getSeason(), winner.getRound());

    if (!existingWinners.isEmpty()) {
      log.debug("Race winner already exists for season {} round {}",
        winner.getSeason(), winner.getRound());
      return existingWinners.get(0);
    }

    if (winner.getDriver() != null) {
      final Optional<com.f1.seasonchampions.model.Driver> existingDriver =
        this.driverRepository.findById(winner.getDriver().getDriverId());

      if (existingDriver.isPresent()) {
        winner.setDriver(existingDriver.get());
      } else {
        winner.setDriver(this.driverRepository.save(winner.getDriver()));
      }
    }

    if (winner.getConstructor() != null) {
      final String constructorId = winner.getConstructor().getConstructorId();
      final Optional<com.f1.seasonchampions.model.Constructor> existingConstructor =
        this.constructorRepository.findByConstructorId(constructorId);

      if (existingConstructor.isPresent()) {
        winner.setConstructor(existingConstructor.get());
      } else {
        winner.setConstructor(this.constructorRepository.save(winner.getConstructor()));
      }
    }

    return this.raceWinnerRepository.save(winner);
  }

  @Override
  public boolean hasCompleteDataForYear(final int year) {
    final List<RaceWinner> existingWinners = this.getRaceWinners(year);
    // TODO: Implement proper validation based on expected number of races per season
    return !existingWinners.isEmpty();
  }
}
