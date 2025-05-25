package com.f1.seasonchampions.service.seasonchampion;

import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalSeasonChampionService implements SeasonChampionService {

  private final SeasonChampionRepository seasonChampionRepository;

  @Override
  @Transactional(readOnly = true)
  public List<SeasonChampion> getSeasonChampions(final SeasonRangeRequest request) {
    log.info(
        "Fetching season champions from local database: {} to {}",
        request.getStartYear(),
        request.getEndYear());

    return this.seasonChampionRepository.findBySeasonBetweenOrderBySeason(
        String.valueOf(request.getStartYear()), String.valueOf(request.getEndYear()));
  }

  @Override
  @Transactional
  public SeasonChampion saveChampion(final SeasonChampion champion) {
    log.debug("Saving champion to database: {}", champion);
    return this.seasonChampionRepository.save(champion);
  }

  public boolean hasCompleteDataForRange(final SeasonRangeRequest request) {
    final List<SeasonChampion> existingChampions = this.getSeasonChampions(request);
    return existingChampions.size() == (request.getEndYear() - request.getStartYear() + 1);
  }
}
