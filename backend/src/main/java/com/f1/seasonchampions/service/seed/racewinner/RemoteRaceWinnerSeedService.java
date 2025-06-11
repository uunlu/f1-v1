package com.f1.seasonchampions.service.seed.racewinner;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.service.F1RaceDataFetchingService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteRaceWinnerSeedService implements RaceWinnerSeedService {

  private final F1RaceDataFetchingService f1RaceDataFetchingService;

  @Override
  public List<RaceWinner> getRaceWinners(final int year) {
    log.info("Fetching race winners from remote API for year: {}", year);

    try {
      final List<RaceWinner> winners = this.f1RaceDataFetchingService.fetchRaceWinnersForYear(year);
      return winners != null ? winners : Collections.emptyList();
    } catch (final Exception e) {
      log.error("Failed to fetch race winners for year {}: {}", year, e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public RaceWinner saveRaceWinner(final RaceWinner winner) {
    return winner; // Remote service doesn't save anything
  }

  @Override
  public boolean hasCompleteDataForYear(final int year) {
    return false; // Remote service always attempts to fetch fresh data
  }
}
