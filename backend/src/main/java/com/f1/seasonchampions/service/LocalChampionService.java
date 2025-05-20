package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalChampionService implements ChampionService {
    private final SeasonChampionRepository seasonChampionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SeasonChampion> getSeasonChampions(SeasonRangeRequest request) {
        log.info("Fetching season champions from local database: {} to {}",
                request.getStartYear(), request.getEndYear());

        return seasonChampionRepository.findBySeasonBetweenOrderBySeason(
                String.valueOf(request.getStartYear()),
                String.valueOf(request.getEndYear()));
    }

    @Override
    @Transactional
    public SeasonChampion saveChampion(SeasonChampion champion) {
        log.debug("Saving champion to database: {}", champion);
        return seasonChampionRepository.save(champion);
    }

    public boolean hasCompleteDataForRange(SeasonRangeRequest request) {
        List<SeasonChampion> existingChampions = getSeasonChampions(request);
        return existingChampions.size() == (request.getEndYear() - request.getStartYear() + 1);
    }
}