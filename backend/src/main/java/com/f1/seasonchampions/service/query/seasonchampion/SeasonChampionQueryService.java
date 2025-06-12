package com.f1.seasonchampions.service.query.seasonchampion;

import com.f1.seasonchampions.dto.SeasonChampionListItem;
import com.f1.seasonchampions.dto.SeasonChampionListItemImpl;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import com.f1.seasonchampions.service.SeasonStatusService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonChampionQueryService {
  private final SeasonChampionRepository seasonChampionRepository;
  private final RaceWinnerRepository raceWinnerRepository;
  private final SeasonStatusService seasonStatusService;

  @Cacheable("all-seasons-cache")
  @NotNull
  public List<SeasonChampionListItem> getAllSeasons() {
    log.info("Fetching all seasons with completion status");

    // Get basic season data from repository
    final List<SeasonChampionListItem> basicSeasons =
        this.seasonChampionRepository.findAllSeasonChampionListItems();

    // Enhance each season with completion status
    return basicSeasons.stream()
        .map(this::enhanceWithCompletionStatus)
        .collect(Collectors.toList());
  }

  /** Enhances a season item with completion status by checking race count and season status. */
  private SeasonChampionListItem enhanceWithCompletionStatus(final SeasonChampionListItem season) {
    try {
      // Count completed races for this season
      final int completedRaces =
          this.raceWinnerRepository.getRaceWinnerBySeason(season.getSeason()).size();

      // Determine if season is completed using existing logic
      final boolean isCompleted =
          this.seasonStatusService.isSeasonConcluded(season.getSeason(), completedRaces);

      log.debug(
          "Season {} has {} completed races and is {}",
          season.getSeason(),
          completedRaces,
          isCompleted ? "completed" : "ongoing");

      // Create new implementation with completion status
      return new SeasonChampionListItemImpl(
          season.getSeason(), season.getDriver(), season.getConstructor(), isCompleted);
    } catch (Exception e) {
      log.warn(
          "Failed to determine completion status for season {}: {}",
          season.getSeason(),
          e.getMessage());
      // Return with completion status as false if we can't determine it
      return new SeasonChampionListItemImpl(
          season.getSeason(), season.getDriver(), season.getConstructor(), false);
    }
  }
}
