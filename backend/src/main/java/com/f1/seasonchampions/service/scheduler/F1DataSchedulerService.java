package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.dto.Race;
import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.dto.ResultsByYearResponse;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.RateLimitedApiClientService;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seed.seasonchampion.SeasonChampionSeedService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class F1DataSchedulerService {
  private final RaceWinnerSeedService raceWinnerService;
  private final RaceWinnerRepository raceResultRepository;
  private final SeasonChampionSeedService seasonChampionSeedService;
  private final RateLimitedApiClientService rateLimitedApiClient;

  @Value("${f1.season.start-year:2005}")
  private int startYear;

  @Value("${f1.api.base-url:https://api.jolpi.ca/ergast/f1}")
  private String apiBaseUrl;

  public F1DataSchedulerService(
      final RaceWinnerSeedService raceWinnerService,
      final RaceWinnerRepository raceResultRepository,
      final SeasonChampionSeedService seasonChampionSeedService,
      final RateLimitedApiClientService rateLimitedApiClient) {
    this.raceWinnerService = raceWinnerService;
    this.raceResultRepository = raceResultRepository;
    this.seasonChampionSeedService = seasonChampionSeedService;
    this.rateLimitedApiClient = rateLimitedApiClient;
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

      // Force remote fetch for sync operations to ensure fresh data
      log.info("Forcing remote API fetch for sync operation to get latest data");
      final var currentYearWinners = this.fetchRaceWinnersForYear(currentYear);

      if (currentYearWinners.isEmpty()) {
        log.info("No race winners found from remote API");
        return new RaceSyncResult(0, List.of(), true, "No race winners found from remote API");
      }

      int newRacesProcessed = 0;

      for (RaceWinner raceWinner : currentYearWinners) {
        final int roundNum = Integer.parseInt(raceWinner.getRound());
        if (lastProcessedRound.isEmpty() || roundNum > lastProcessedRound.get()) {
          // Use the seed service to properly save with entity relationships
          this.raceWinnerService.saveRaceWinner(raceWinner);
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

    final List<String> allUpdatedRaces = new ArrayList<>();
    int totalUpdates = 0;
    boolean overallSuccess = true;

    try {
      // Simple for loop from 2005 to current year
      for (int year = historicalStartYear; year <= currentYear; year++) {
        log.info("Syncing races for year {}", year);

        try {
          // Check if we already have complete data for this year
          if (this.raceWinnerService.hasCompleteDataForYear(year)) {
            log.debug("Year {} already has complete race data, skipping", year);
            continue;
          }

          // Use the same logic as fetchRaceWinnersForYear
          final List<RaceWinner> raceWinners = this.fetchRaceWinnersForYear(year);
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

  /**
   * Fetches race winners for a specific year using round-by-round API calls. This is a copy of the
   * logic from RemoteRaceWinnerSeedService.
   *
   * @param year The year to fetch race winners for
   * @return List of race winners for the year
   */
  private List<RaceWinner> fetchRaceWinnersForYear(final int year) {
    final List<RaceWinner> allWinners = new ArrayList<>();
    int round = 1;
    int consecutiveEmptyRounds = 0;
    final int maxConsecutiveEmptyRounds = 3; // Stop after 3 consecutive empty rounds

    log.info("Starting round-by-round fetching for year {}", year);

    while (consecutiveEmptyRounds < maxConsecutiveEmptyRounds && round <= 30) {
      final String url = String.format("%s/%d/%d/results.json", this.apiBaseUrl, year, round);

      log.debug("Fetching round {} from URL: {}", round, url);

      try {
        final ResponseEntity<ResultsByYearResponse> response =
            this.rateLimitedApiClient.executeRateLimitedRequest(url, ResultsByYearResponse.class);

        final ResultsByYearResponse result = response.getBody();
        if (result == null
            || result.getMrData() == null
            || result.getMrData().getRaceTable() == null
            || result.getMrData().getRaceTable().getRaces() == null
            || result.getMrData().getRaceTable().getRaces().isEmpty()) {
          log.debug("No race data found for round {} in year {}", round, year);
          consecutiveEmptyRounds++;
        } else {
          // Reset counter when we find data
          consecutiveEmptyRounds = 0;

          final List<Race> races = result.getMrData().getRaceTable().getRaces();
          log.debug("Received {} races for round {} in year {}", races.size(), round, year);

          final List<RaceWinner> winners =
              races.stream().map(this::mapToRaceWinner).flatMap(List::stream).toList();

          allWinners.addAll(winners);

          log.debug(
              "Round {} completed for year {}. Winners so far: {}", round, year, allWinners.size());
        }

      } catch (final RestClientException e) {
        log.debug(
            "Request failed for round {} in year {}: {} - trying next round",
            round,
            year,
            e.getMessage());
        consecutiveEmptyRounds++;
      }

      round++; // Always move to next round
    }

    log.info(
        "Round-by-round fetching completed for year {}. Total winners fetched: {} from {} rounds checked",
        year,
        allWinners.size(),
        round - 1);
    return allWinners;
  }

  @NotNull
  private List<RaceWinner> mapToRaceWinner(final Race race) {
    if (race == null) {
      log.warn("Race object is null");
      return Collections.emptyList();
    }

    final var results = race.getResults();
    if (results == null || results.isEmpty()) {
      log.warn("No results found for race round {}", race.getRound());
      return Collections.emptyList();
    }

    final var winnerResult =
        results.stream()
            .filter(result -> result != null && "1".equals(result.getPosition()))
            .findFirst();

    if (winnerResult.isEmpty()) {
      log.warn("No winner found in race round {} for season {}", race.getRound(), race.getSeason());
      return Collections.emptyList();
    }

    final var winner = winnerResult.get();
    final var constructor = winner.getConstructor();
    final var driver = winner.getDriver();
    final var time = winner.getTime();

    if (driver == null || constructor == null) {
      log.warn(
          "Skipping incomplete winner data - Round: {}, Season: {}, Has Driver: {}, Has Constructor: {}",
          race.getRound(),
          race.getSeason(),
          driver != null,
          constructor != null);
      return Collections.emptyList();
    }

    final RaceWinner raceWinner = new RaceWinner();
    raceWinner.setRound(race.getRound());
    raceWinner.setSeason(race.getSeason());
    raceWinner.setTime(time.getTime());
    raceWinner.setConstructor(
        new Constructor(
            constructor.getConstructorId(), constructor.getName(), constructor.getNationality()));
    raceWinner.setDriver(
        new Driver(
            driver.getDriverId(),
            driver.getPermanentNumber(),
            driver.getCode(),
            driver.getGivenName(),
            driver.getFamilyName(),
            driver.getDateOfBirth(),
            driver.getNationality()));

    log.info(
        "Winner mapped successfully - Round: {}, Season: {}, Driver: {} {}, Constructor: {}",
        raceWinner.getRound(),
        raceWinner.getSeason(),
        driver.getGivenName(),
        driver.getFamilyName(),
        constructor.getName());

    return Collections.singletonList(raceWinner);
  }
}
