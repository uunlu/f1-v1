package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import jakarta.transaction.Transactional;
import java.time.Year;
import java.util.Comparator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class F1DataSchedulerService {
  private final RaceWinnerSeedService raceWinnerService;
  private final RaceWinnerRepository raceResultRepository;

  public F1DataSchedulerService(
      final RaceWinnerSeedService raceWinnerService,
      final RaceWinnerRepository raceResultRepository) {
    this.raceWinnerService = raceWinnerService;
    this.raceResultRepository = raceResultRepository;
  }

  @Scheduled(cron = "${f1.data.sync.cron:0 0 12 * * ?}")
  @Transactional
  public void syncLatestF1RaceResult() {
    log.info("Starting F1DataSchedulerService syncLatestF1RaceResult");

    try {
      final var currentYear = Year.now().getValue();

      final Optional<Integer> lastProcessedRound = this.getLastProcessedRound(currentYear);

      log.info(
          "Last processed round for year {}: {}",
          currentYear,
          lastProcessedRound.isPresent() ? lastProcessedRound.get() : "none");

      log.info("Last processed race round: {}", lastProcessedRound);

      final var currentYearWinners = this.raceWinnerService.getRaceWinners(currentYear);
      if (currentYearWinners.isEmpty()) {
        log.info("No race winners found");
        return;
      }

      int newRacesProcessed = 0;

      for (RaceWinner raceWinner : currentYearWinners) {
        if (!lastProcessedRound.isPresent()
            || Integer.parseInt(raceWinner.getRound()) > lastProcessedRound.get()) {
          this.raceResultRepository.save(raceWinner);
          newRacesProcessed++;
          log.info(
              "Saved new race winner result: Season {} Round {} ",
              raceWinner.getSeason(),
              raceWinner.getRound());
        }
      }

      if (newRacesProcessed > 0) {
        log.info("Successfully sync {} latest F1 race results", newRacesProcessed);
      } else {
        log.info("No race winners found");
      }
    } catch (Exception e) {
      log.error("F1DataSchedulerService error sync race data: {}", e.getMessage(), e);
    }
  }

  private Optional<Integer> getLastProcessedRound(final int year) {
    return this.raceResultRepository.getRaceWinnerBySeason(String.valueOf(year)).stream()
        .map(raceWinner -> Integer.parseInt(raceWinner.getRound()))
        .max(Comparator.naturalOrder());
  }
}
