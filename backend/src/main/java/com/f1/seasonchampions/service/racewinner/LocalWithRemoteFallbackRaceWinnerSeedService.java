package com.f1.seasonchampions.service.racewinner;

import com.f1.seasonchampions.model.RaceWinner;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class LocalWithRemoteFallbackRaceWinnerSeedService implements RaceWinnerSeedService {
  private final LocalRaceWinnerSeedService localService;
  private final RemoteRaceWinnerSeedService remoteService;

  @Override
  @Transactional
  public List<RaceWinner> getRaceWinners(final int year) {
    if (this.localService.hasCompleteDataForYear(year)) {
      log.info("Returning race winners from local database for year: {}", year);
      return this.localService.getRaceWinners(year);
    }

    log.info("Fetching race winners from remote API for year: {}", year);
    final List<RaceWinner> remoteWinners = this.remoteService.getRaceWinners(year);

    for (final RaceWinner winner : remoteWinners) {
      log.info("Saving race winners from remote API for year: {}", year);
      this.localService.saveRaceWinner(winner);
    }

    return remoteWinners;
  }

  @Override
  @Transactional
  public RaceWinner saveRaceWinner(final RaceWinner winner) {
    return this.localService.saveRaceWinner(winner);
  }

  @Override
  public boolean hasCompleteDataForYear(final int year) {
    return this.localService.hasCompleteDataForYear(year);
  }
}
