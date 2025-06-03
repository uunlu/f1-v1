package com.f1.seasonchampions.service.query.racewinner;

import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.dto.RaceWinnerListItemImpl;
import com.f1.seasonchampions.dto.RaceWinnerSeasonResponse;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.SeasonStatusService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RaceWinnerQueryService {
  private final RaceWinnerRepository raceWinnerRepository;
  private final DriverRepository driverRepository;
  private final ConstructorRepository constructorRepository;
  private final SeasonStatusService seasonStatusService;

  /**
   * Gets race winners for a specific season with champion status information. Handles the edge case
   * for ongoing seasons where no driver should be marked as champion until the season concludes.
   *
   * @param year the season year
   * @return list of race winners with accurate champion status
   */
  @NotNull
  public List<RaceWinnerListItem> getWinnersBySeason(final int year) {
    final String season = String.valueOf(year);
    log.info("Fetching race winners with champion status for season {}", season);

    final List<RaceWinnerListItem> originalWinners =
        this.raceWinnerRepository.findRaceWinnersWithConstructors(season);

    // Check if this is an ongoing season
    if (!this.seasonStatusService.isSeasonConcluded(season, originalWinners.size())) {
      log.info("Season {} is ongoing - adjusting champion status", season);
      return this.handleOngoingSeasonChampionStatus(originalWinners);
    }

    log.debug("Season {} is concluded - preserving champion status", season);
    return originalWinners;
  }

  /**
   * Handles the special case for ongoing seasons by creating mutable implementations and clearing
   * champion status for all drivers.
   *
   * @param originalWinners the original race winners from the repository
   * @return modified race winners with champion status cleared for ongoing seasons
   */
  private List<RaceWinnerListItem> handleOngoingSeasonChampionStatus(
      final List<RaceWinnerListItem> originalWinners) {
    return originalWinners.stream()
        .map(
            winner ->
                new RaceWinnerListItemImpl(
                    winner.getSeasonName(),
                    winner.getRound(),
                    false, // Clear champion status for ongoing seasons
                    winner.getDriver(),
                    winner.getSeasonDriverId(),
                    winner.getSeasonConstructorId(),
                    winner.getConstructorName()))
        .collect(Collectors.toList());
  }

  /**
   * Gets race winners for a specific season with full season metadata including champion status and
   * season conclusion status.
   *
   * @param year the season year
   * @return response with race winners and season metadata
   */
  @NotNull
  public RaceWinnerSeasonResponse getWinnersWithSeasonMetadata(final int year) {
    final String season = String.valueOf(year);
    log.info("Fetching race winners with full season metadata for season {}", season);

    final List<RaceWinnerListItem> originalWinners =
        this.raceWinnerRepository.findRaceWinnersWithConstructors(season);

    final boolean isSeasonConcluded =
        this.seasonStatusService.isSeasonConcluded(season, originalWinners.size());

    List<RaceWinnerListItem> finalWinners;
    if (!isSeasonConcluded) {
      log.info("Season {} is ongoing - adjusting champion status", season);
      finalWinners = this.handleOngoingSeasonChampionStatus(originalWinners);
    } else {
      log.debug("Season {} is concluded - preserving champion status", season);
      finalWinners = originalWinners;
    }

    final boolean hasChampion = finalWinners.stream().anyMatch(RaceWinnerListItem::isChampion);

    return new RaceWinnerSeasonResponse(
        season, isSeasonConcluded, hasChampion, finalWinners.size(), finalWinners);
  }
}
