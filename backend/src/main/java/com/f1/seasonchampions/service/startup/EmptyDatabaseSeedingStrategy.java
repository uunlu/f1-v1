package com.f1.seasonchampions.service.startup;

import com.f1.seasonchampions.repository.RaceWinnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Seeding strategy that checks if the database is empty of race data. Implements the Strategy
 * pattern for determining seeding necessity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmptyDatabaseSeedingStrategy implements F1DataSeedingStrategy {

  private final RaceWinnerRepository raceWinnerRepository;

  @Override
  public boolean isSeedingNeeded() {
    final long raceCount = this.raceWinnerRepository.count();
    log.debug("Current race winner count in database: {}", raceCount);

    final boolean seedingNeeded = raceCount == 0;
    log.info("Seeding needed: {} (race count: {})", seedingNeeded, raceCount);

    return seedingNeeded;
  }
}
