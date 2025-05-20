package com.f1.seasonchampions.service;

import com.f1.seasonchampions.exception.InvalidInputException;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class LocalWithRemoteFallbackChampionService implements ChampionService {
    private final LocalChampionService localService;
    private final RemoteChampionService remoteService;

    @Override
    @Transactional
    public List<SeasonChampion> getSeasonChampions(SeasonRangeRequest request) {
        if (request.getStartYear() > request.getEndYear()) {
            throw new InvalidInputException("Start year cannot be greater than end year");
        }

        // Check if we have all data locally
        if (localService.hasCompleteDataForRange(request)) {
            log.info("Returning champions from local database");
            return localService.getSeasonChampions(request);
        }

        // Get what we have locally
        List<SeasonChampion> localChampions = localService.getSeasonChampions(request);
        Set<String> existingSeasons = localChampions.stream()
                .map(SeasonChampion::getSeason)
                .collect(Collectors.toSet());

        List<SeasonChampion> result = new ArrayList<>(localChampions);

        // Fetch missing years from remote
        for (int year = request.getStartYear(); year <= request.getEndYear(); year++) {
            String yearStr = String.valueOf(year);
            if (existingSeasons.contains(yearStr)) {
                continue;
            }

            // Create a single year request
            SeasonRangeRequest singleYearRequest = new SeasonRangeRequest(year, year);
            List<SeasonChampion> remoteChampions = remoteService.getSeasonChampions(singleYearRequest);

            for (SeasonChampion champion : remoteChampions) {
                // Save to local database
                SeasonChampion savedChampion = localService.saveChampion(champion);
                result.add(savedChampion);
            }
        }

        // Sort by season
        result.sort(Comparator.comparing(SeasonChampion::getSeason));

        log.info("Retrieved {} champions from combined sources", result.size());
        return result;
    }

    @Override
    @Transactional
    public SeasonChampion saveChampion(SeasonChampion champion) {
        return localService.saveChampion(champion);
    }
}