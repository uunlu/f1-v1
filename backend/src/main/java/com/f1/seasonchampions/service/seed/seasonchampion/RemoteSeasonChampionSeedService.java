package com.f1.seasonchampions.service.seed.seasonchampion;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.service.F1ApiClient;
import com.f1.seasonchampions.service.RateLimitedApiClientService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteSeasonChampionSeedService implements SeasonChampionSeedService {

  private final F1ApiClient f1ApiClient;
  private final RateLimitedApiClientService rateLimitedApiClient;

  @Retryable(
      retryFor = RestClientException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 3000, multiplier = 2))
  @Override
  public List<SeasonChampion> getSeasonChampions(final SeasonRangeRequest request) {
    log.info(
        "Fetching season champions from remote API: {} to {}",
        request.getStartYear(),
        request.getEndYear());

    final List<SeasonChampion> champions = new ArrayList<>();

    for (int year = request.getStartYear(); year <= request.getEndYear(); year++) {
      final int currentYear = year;

      try {
        final SeasonChampion champion =
            this.rateLimitedApiClient.executeRateLimitedOperation(
                () -> this.fetchChampionForYear(currentYear),
                "Fetch champion for year " + currentYear);

        if (champion != null) {
          champions.add(champion);
        }
      } catch (Exception e) {
        log.error("Failed to fetch champion for year {}: {}", currentYear, e.getMessage());
      }
    }

    return champions;
  }

  @Override
  public List<SeasonChampion> getAllSeasonChampions() {
    log.info("Fetching all season champions from remote API");

    // TODO: read from yml file
    final int startYear = 2005;
    final int endYear = java.time.Year.now().getValue();

    return this.getSeasonChampions(new SeasonRangeRequest(startYear, endYear));
  }

  @Override
  public SeasonChampion saveChampion(final SeasonChampion champion) {
    // Remote service doesn't save anything
    return champion;
  }

  private SeasonChampion fetchChampionForYear(final int year) {
    return this.f1ApiClient.fetchChampionForSeason(year);
  }
}
