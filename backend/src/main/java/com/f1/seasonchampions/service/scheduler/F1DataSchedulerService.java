package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seed.seasonchampion.SeasonChampionSeedService;
import jakarta.transaction.Transactional;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class F1DataSchedulerService {
  private final RaceWinnerSeedService raceWinnerService;
  private final RaceWinnerRepository raceResultRepository;
  private final SeasonChampionSeedService seasonChampionSeedService;

  public F1DataSchedulerService(
      final RaceWinnerSeedService raceWinnerService,
      final RaceWinnerRepository raceResultRepository,
      final SeasonChampionSeedService seasonChampionSeedService) {
    this.raceWinnerService = raceWinnerService;
    this.raceResultRepository = raceResultRepository;
    this.seasonChampionSeedService = seasonChampionSeedService;
  }

  @Scheduled(cron = "${f1.data.sync.cron:0 0 12 * * ?}")
  @Transactional
  public RaceSyncResult syncLatestF1RaceResult() {
    log.info("Starting F1DataSchedulerService syncLatestF1RaceResult");

    final List<String> updated = new ArrayList<>();
    try {
      final int currentYear = Year.now().getValue();

      final Optional<Integer> lastProcessedRound = this.getLastProcessedRound(currentYear);

      log.info(
          "Last processed round for year {}: {}",
          currentYear,
          lastProcessedRound.isPresent() ? lastProcessedRound.get() : "none");

      final var currentYearWinners = this.raceWinnerService.getRaceWinners(currentYear);
      if (currentYearWinners.isEmpty()) {
        log.info("No race winners found");
        return new RaceSyncResult(0, List.of(), true, "No race winners found");
      }

      int newRacesProcessed = 0;

      for (RaceWinner raceWinner : currentYearWinners) {
        final int roundNum = Integer.parseInt(raceWinner.getRound());
        if (lastProcessedRound.isEmpty() || roundNum > lastProcessedRound.get()) {
          this.raceResultRepository.save(raceWinner);
          newRacesProcessed++;
          updated.add("Season " + raceWinner.getSeason() + " Round " + raceWinner.getRound());
          log.info(
              "Saved new race winner result: Season {} Round {}",
              raceWinner.getSeason(),
              raceWinner.getRound());
        }
      }

      if (newRacesProcessed > 0) {
        log.info("Successfully synced {} latest F1 race results", newRacesProcessed);
        return new RaceSyncResult(newRacesProcessed, updated, true, "Sync completed successfully");
      } else {
        log.info("No new race winners to sync");
        return new RaceSyncResult(0, List.of(), true, "No new race winners to sync");
      }

    } catch (Exception e) {
      log.error("F1DataSchedulerService error sync race data: {}", e.getMessage(), e);
      return new RaceSyncResult(0, List.of(), false, "Sync failed: " + e.getMessage());
    }
  }

  private Optional<Integer> getLastProcessedRound(final int year) {
    return this.raceResultRepository.getRaceWinnerBySeason(String.valueOf(year)).stream()
        .map(raceWinner -> Integer.parseInt(raceWinner.getRound()))
        .max(Comparator.naturalOrder());
  }

  // --- NEW scheduled method for syncing season champions ---
  @Scheduled(cron = "${f1.seasonchampions.sync.cron:0 30 12 * * ?}")
  @Transactional
  public RaceSyncResult syncSeasonChampions() {
    log.info("Starting F1DataSchedulerService syncSeasonChampions");

    final List<String> updated = new ArrayList<>();
    try {
      final int startYear = 2005; // you can externalize this if you want
      final int endYear = Year.now().getValue();

      final List<SeasonChampion> champions =
          this.seasonChampionSeedService.getSeasonChampions(
              new SeasonRangeRequest(startYear, endYear));

      if (champions.isEmpty()) {
        log.info("No season champions found");
        return new RaceSyncResult(0, List.of(), true, "No season champions found");
      }

      int newChampionsSaved = 0;
      for (SeasonChampion champion : champions) {
        final SeasonChampion saved = this.seasonChampionSeedService.saveChampion(champion);
        newChampionsSaved++;
        updated.add("SeasonChampion for season " + saved.getSeason());
        log.info("Saved/updated season champion for season {}", saved.getSeason());
      }

      if (newChampionsSaved > 0) {
        log.info("Successfully synced {} season champions", newChampionsSaved);
        return new RaceSyncResult(newChampionsSaved, updated, true, "Sync completed successfully");
      } else {
        log.info("No new season champions to sync");
        return new RaceSyncResult(0, List.of(), true, "No new season champions to sync");
      }

    } catch (Exception e) {
      log.error("F1DataSchedulerService error sync season champions: {}", e.getMessage(), e);
      return new RaceSyncResult(0, List.of(), false, "Sync failed: " + e.getMessage());
    }
  }
}
