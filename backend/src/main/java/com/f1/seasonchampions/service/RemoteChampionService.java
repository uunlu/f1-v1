package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteChampionService implements ChampionService {
    private final F1ApiClient f1ApiClient;
    private RateLimiter rateLimiter;

    @PostConstruct
    public void init() {
        // Configure rate limiter
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(1)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        rateLimiter = registry.rateLimiter("apiRateLimiter");
    }

    @Override
    public List<SeasonChampion> getSeasonChampions(SeasonRangeRequest request) {
        log.info("Fetching season champions from remote API: {} to {}",
                request.getStartYear(), request.getEndYear());

        List<SeasonChampion> champions = new ArrayList<>();

        for (int year = request.getStartYear(); year <= request.getEndYear(); year++) {
            final int currentYear = year;

            // Wrap the API call with rate limiter
            Supplier<SeasonChampion> rateLimitedCall = RateLimiter
                    .decorateSupplier(rateLimiter, () -> fetchChampionForYear(currentYear));

            try {
                SeasonChampion champion = rateLimitedCall.get();
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
    public SeasonChampion saveChampion(SeasonChampion champion) {
        // Remote service doesn't save anything
        return champion;
    }

    private SeasonChampion fetchChampionForYear(int year) {
        // Implementation from the original service
        return f1ApiClient.fetchChampionForSeason(year);
    }
}
