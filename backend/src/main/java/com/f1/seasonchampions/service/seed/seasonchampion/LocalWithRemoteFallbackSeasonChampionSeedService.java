package com.f1.seasonchampions.service.seed.seasonchampion;

import com.f1.seasonchampions.exception.InvalidInputException;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class LocalWithRemoteFallbackSeasonChampionSeedService implements SeasonChampionSeedService {

  private final LocalSeasonChampionSeedService localService;
  private final RemoteSeasonChampionSeedService remoteService;

  @Override
  @Transactional
  @NotNull
  public List<SeasonChampion> getSeasonChampions(@NotNull final SeasonRangeRequest request) {
    if (request.getStartYear() > request.getEndYear()) {
      throw new InvalidInputException("Start year cannot be greater than end year");
    }

    if (this.localService.hasCompleteDataForRange(request)) {
      log.info("Returning champions from local database");
      return this.localService.getSeasonChampions(request);
    }

    final List<SeasonChampion> localChampions = this.localService.getSeasonChampions(request);
    final Set<String> existingSeasons =
        localChampions.stream().map(SeasonChampion::getSeason).collect(Collectors.toSet());

    final List<SeasonChampion> result = new ArrayList<>(localChampions);

    for (int year = request.getStartYear(); year <= request.getEndYear(); year++) {
      final String yearStr = String.valueOf(year);
      if (existingSeasons.contains(yearStr)) {
        continue;
      }

      final SeasonRangeRequest singleYearRequest = new SeasonRangeRequest(year, year);
      final List<SeasonChampion> remoteChampions =
          this.remoteService.getSeasonChampions(singleYearRequest);

      for (final SeasonChampion champion : remoteChampions) {
        final SeasonChampion savedChampion = this.localService.saveChampion(champion);
        result.add(savedChampion);
      }
    }

    result.sort(Comparator.comparing(SeasonChampion::getSeason));

    log.info("Retrieved {} champions from combined sources", result.size());
    return result;
  }

  @Override
  @Transactional
  @NotNull
  public List<SeasonChampion> getAllSeasonChampions() {
    log.info("Fetching all season champions with local+remote fallback strategy");

    // TODO: read from yml
    final int startYear = 2005;
    final int endYear = java.time.Year.now().getValue();
    final SeasonRangeRequest fullRangeRequest = new SeasonRangeRequest(startYear, endYear);

    // Check if we have complete data locally
    if (this.localService.hasCompleteDataForRange(fullRangeRequest)) {
      log.info("Returning all champions from local database (complete data)");
      return this.localService.getAllSeasonChampions();
    }

    log.info(
        "Local data incomplete, fetching from combined sources for years {}-{}",
        startYear,
        endYear);
    return this.getSeasonChampions(fullRangeRequest);
  }

  @Override
  @Transactional
  @NotNull
  public SeasonChampion saveChampion(@NotNull final SeasonChampion champion) {
    return this.localService.saveChampion(champion);
  }
}
