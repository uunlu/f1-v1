package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.F1RaceDataFetchingService;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seed.seasonchampion.SeasonChampionSeedService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
  private final F1RaceDataFetchingService f1RaceDataFetchingService;

  @Value("${f1.season.start-year:2005}")
  private int startYear;

  @Value("${f1.season.end-year:#{T(java.time.Year).now().getValue()}}")
  private int endYear;

  public F1DataSchedulerService(
      final RaceWinnerSeedService raceWinnerService,
      final RaceWinnerRepository raceResultRepository,
      final SeasonChampionSeedService seasonChampionSeedService,
      final F1RaceDataFetchingService f1RaceDataFetchingService) {
    this.raceWinnerService = raceWinnerService;
    this.raceResultRepository = raceResultRepository;
    this.seasonChampionSeedService = seasonChampionSeedService;
    this.f1RaceDataFetchingService = f1RaceDataFetchingService;
  }

  @PostConstruct
  public void init() {
    log.info(
        "F1DataSchedulerService initialized with start year: {} and end year: {}",
        this.startYear,
        this.endYear);
  }

  @Scheduled(cron = "${f1.data.sync.cron:0 0 12 * * ?}")
  @Transactional
  public RaceSyncResult syncLatestF1RaceResult() {
    log.info("Starting scheduled F1 race result synchronization");

    final int currentYear = Year.now().getValue();
    final List<String> updatedRaces = new ArrayList<>();
    int totalUpdates = 0;

    try {
      final Optional<Integer> lastProcessedRound = this.getLastProcessedRound(currentYear);

      if (lastProcessedRound.isPresent()) {
        log.info(
            "Last processed round for {}: {}. Checking for new races...",
            currentYear,
            lastProcessedRound.get());
      } else {
        log.info("No previous data found for {}. Starting from the beginning.", currentYear);
      }

      // Use the shared service to fetch race winners for the current year
      final List<RaceWinner> raceWinners =
          this.f1RaceDataFetchingService.fetchRaceWinnersForYear(currentYear);

      for (RaceWinner raceWinner : raceWinners) {
        try {
          final RaceWinner savedWinner = this.raceWinnerService.saveRaceWinner(raceWinner);
          if (savedWinner != null) {
            totalUpdates++;
            updatedRaces.add(
                "Season " + raceWinner.getSeason() + " Round " + raceWinner.getRound());
            log.info(
                "Updated race result: Season {} Round {}",
                raceWinner.getSeason(),
                raceWinner.getRound());
          }
        } catch (Exception e) {
          log.warn(
              "Failed to save race winner for Season {} Round {}: {}",
              raceWinner.getSeason(),
              raceWinner.getRound(),
              e.getMessage());
        }
      }

      log.info("Scheduled sync completed. Total updates: {}", totalUpdates);
      return new RaceSyncResult(
          totalUpdates, updatedRaces, true, "Synchronization completed successfully");

    } catch (Exception e) {
      log.error("Error during scheduled sync", e);
      return new RaceSyncResult(0, List.of(), false, "Sync failed: " + e.getMessage());
    }
  }

  private Optional<Integer> getLastProcessedRound(final int year) {
    return this.raceResultRepository.findMaxRoundByYear(String.valueOf(year));
  }

  @Scheduled(cron = "${f1.seasonchampions.sync.cron:0 30 12 * * ?}")
  @Transactional
  public RaceSyncResult syncSeasonChampions() {
    log.info("Starting scheduled season champions synchronization");

    try {
      final SeasonRangeRequest request = new SeasonRangeRequest(this.startYear, this.endYear);
      final List<SeasonChampion> champions =
          this.seasonChampionSeedService.getSeasonChampions(request);

      champions.sort(Comparator.comparing(SeasonChampion::getSeason));

      final List<String> championDetails = new ArrayList<>();
      for (SeasonChampion champion : champions) {
        championDetails.add(
            "Season " + champion.getSeason() + ": " + champion.getDriver().getFamilyName());
      }

      log.info("Season champions sync completed. Total champions: {}", champions.size());
      return new RaceSyncResult(
          champions.size(), championDetails, true, "Season champions sync completed successfully");

    } catch (Exception e) {
      log.error("Error during season champions sync", e);
      return new RaceSyncResult(
          0, List.of(), false, "Season champions sync failed: " + e.getMessage());
    }
  }

  @Transactional
  public RaceSyncResult syncAllHistoricalRaces() {
    log.info("Starting historical race sync from {} to {}", this.startYear, this.endYear);

    final List<String> allUpdatedRaces = new ArrayList<>();
    int totalUpdates = 0;
    boolean overallSuccess = true;

    try {
      // Simple for loop from start year to end year
      for (int year = this.startYear; year <= this.endYear; year++) {
        log.info("Syncing races for year {}", year);

        try {
          // Check if we already have complete data for this year
          if (this.raceWinnerService.hasCompleteDataForYear(year)) {
            log.debug("Year {} already has complete race data, skipping", year);
            continue;
          }

          // Use the shared service to fetch race winners
          final List<RaceWinner> raceWinners =
              this.f1RaceDataFetchingService.fetchRaceWinnersForYear(year);
          int savedCount = 0;

          for (RaceWinner raceWinner : raceWinners) {
            try {
              // Use the seed service which handles entity dependencies properly
              this.raceWinnerService.saveRaceWinner(raceWinner);
              savedCount++;
              allUpdatedRaces.add(
                  "Season " + raceWinner.getSeason() + " Round " + raceWinner.getRound());
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

          totalUpdates += savedCount;
          log.info("Synced {} races for year {}", savedCount, year);

        } catch (Exception e) {
          log.error("Error syncing races for year {}: {}", year, e.getMessage(), e);
          overallSuccess = false;
        }
      }

      final String message =
          overallSuccess
              ? "Historical race sync completed successfully"
              : "Historical race sync completed with some errors";

      return new RaceSyncResult(totalUpdates, allUpdatedRaces, overallSuccess, message);
    } catch (Exception e) {
      log.error("Error syncing historical races", e);
      return new RaceSyncResult(0, List.of(), false, "Sync failed: " + e.getMessage());
    }
  }
}
