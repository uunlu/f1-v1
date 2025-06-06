package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seed.seasonchampion.SeasonChampionSeedService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class F1DataSchedulerService {
  private final RaceWinnerSeedService raceWinnerService;
  private final RaceWinnerRepository raceResultRepository;
  private final SeasonChampionSeedService seasonChampionSeedService;

  @Value("${f1.season.start-year:2005}")
  private int startYear;

  public F1DataSchedulerService(
      final RaceWinnerSeedService raceWinnerService,
      final RaceWinnerRepository raceResultRepository,
      final SeasonChampionSeedService seasonChampionSeedService) {
    this.raceWinnerService = raceWinnerService;
    this.raceResultRepository = raceResultRepository;
    this.seasonChampionSeedService = seasonChampionSeedService;
  }

  @PostConstruct
  public void init() {
    log.info("F1DataSchedulerService initialized with start year: {}", this.startYear);
    log.info("Using race winner service: {}", this.raceWinnerService.getClass().getSimpleName());
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
      final int endYear = Year.now().getValue();

      final List<SeasonChampion> champions =
          this.seasonChampionSeedService.getSeasonChampions(
              new SeasonRangeRequest(this.startYear, endYear));

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

  /**
   * Synchronizes all F1 race results from 2005 to the current year. Intended to be used once during
   * application startup to seed the database.
   *
   * @return Result of the synchronization process
   */
  @Transactional
  public RaceSyncResult syncAllHistoricalRaces() {
    final int currentYear = Year.now().getValue();
    final int historicalStartYear = 2005;
    log.info("Starting historical race sync from {} to {}", historicalStartYear, currentYear);

    try {
      // Create a list of futures for each year
      final List<CompletableFuture<RaceSyncResult>> futures =
          IntStream.rangeClosed(historicalStartYear, currentYear)
              .mapToObj(
                  year ->
                      CompletableFuture.supplyAsync(
                          () -> {
                            log.info("Syncing races for year {}", year);
                            return this.syncRacesForYear(year);
                          }))
              .toList();

      // Wait for all futures to complete
      final List<RaceSyncResult> results = futures.stream().map(CompletableFuture::join).toList();

      // Aggregate results
      final boolean overallSuccess = results.stream().allMatch(RaceSyncResult::success);
      final int totalUpdates = results.stream().mapToInt(RaceSyncResult::updatedCount).sum();
      final List<String> updatedRaces =
          results.stream().flatMap(result -> result.updatedRaces().stream()).toList();

      final String message =
          overallSuccess
              ? "Historical race sync completed successfully"
              : "Historical race sync completed with some errors";

      return new RaceSyncResult(totalUpdates, updatedRaces, overallSuccess, message);
    } catch (Exception e) {
      log.error("Error syncing historical races", e);
      return new RaceSyncResult(0, List.of(), false, "Sync failed: " + e.getMessage());
    }
  }

  /**
   * Synchronizes race results for a specific year.
   *
   * @param year The year to sync race results for
   * @return Result of the synchronization process
   */
  @Transactional
  private RaceSyncResult syncRacesForYear(final int year) {
    try {
      // Check if we already have complete data for this year
      if (this.raceWinnerService.hasCompleteDataForYear(year)) {
        log.debug("Year {} already has complete race data, skipping", year);
        return new RaceSyncResult(0, List.of(), true, "Data already exists for year " + year);
      }

      final List<RaceWinner> raceWinners = this.raceWinnerService.getRaceWinners(year);
      final List<String> updatedRaces = new ArrayList<>();
      int savedCount = 0;

      for (RaceWinner raceWinner : raceWinners) {
        try {
          // The seed service handles duplicate checking internally
          this.raceWinnerService.saveRaceWinner(raceWinner);

          savedCount++;
          updatedRaces.add("Season " + raceWinner.getSeason() + " Round " + raceWinner.getRound());
          log.debug(
              "Saved race winner: Season {} Round {}",
              raceWinner.getSeason(),
              raceWinner.getRound());
        } catch (Exception e) {
          log.warn(
              "Failed to save race winner for Season {} Round {}: {}",
              raceWinner.getSeason(),
              raceWinner.getRound(),
              e.getMessage());
        }
      }

      log.info("Synced {} races for year {}", savedCount, year);
      return new RaceSyncResult(
          savedCount,
          updatedRaces,
          true,
          "Successfully synced " + savedCount + " races for year " + year);

    } catch (Exception e) {
      log.error("Error syncing races for year {}: {}", year, e.getMessage(), e);
      return new RaceSyncResult(
          0, List.of(), false, "Failed to sync year " + year + ": " + e.getMessage());
    }
  }
}
