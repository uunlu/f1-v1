package com.f1.seasonchampions.service;

import java.time.LocalDate;
import java.time.Year;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to determine the status of F1 seasons, particularly whether a season has concluded. This
 * is used to handle the edge case where drivers should not be marked as champions during ongoing
 * seasons, even if they have mathematically secured the championship.
 */
@Service
@Slf4j
public class SeasonStatusService {

  @Value("${f1.season.total-races:23}")
  private int totalRacesInSeason;

  @Value("${f1.season.end-month:12}")
  private int seasonEndMonth;

  /**
   * Determines if a given F1 season has concluded. A season is concluded if: 1. It's a past year 2.
   * It's the current year but we're past the typical season end month (December) 3. All races for
   * the season have been completed
   *
   * @param seasonYear the year of the season to check
   * @param completedRaces the number of races completed in the season
   * @return true if the season has concluded, false if it's ongoing
   */
  public boolean isSeasonConcluded(final String seasonYear, final int completedRaces) {
    final int year = Integer.parseInt(seasonYear);
    final int currentYear = Year.now().getValue();

    log.debug("Checking season status for year {} with {} completed races", year, completedRaces);

    // Past seasons are always concluded
    if (year < currentYear) {
      log.debug("Season {} is in the past, marking as concluded", year);
      return true;
    }

    // Future seasons haven't concluded
    if (year > currentYear) {
      log.debug("Season {} is in the future, marking as not concluded", year);
      return false;
    }

    // Current year - check if we're past the season end month or all races completed
    if (year == currentYear) {
      final int currentMonth = LocalDate.now().getMonthValue();

      if (currentMonth >= this.seasonEndMonth) {
        log.debug(
            "Current season {} has passed end month {}, marking as concluded",
            year,
            this.seasonEndMonth);
        return true;
      }

      // If all races are completed, the season is concluded
      if (completedRaces >= this.totalRacesInSeason) {
        log.debug(
            "Current season {} has completed all {} races, marking as concluded",
            year,
            this.totalRacesInSeason);
        return true;
      }

      log.debug(
          "Current season {} is ongoing with {} of {} races completed",
          year,
          completedRaces,
          this.totalRacesInSeason);
      return false;
    }

    return false;
  }
}
