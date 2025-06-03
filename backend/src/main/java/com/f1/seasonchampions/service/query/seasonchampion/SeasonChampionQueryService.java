package com.f1.seasonchampions.service.query.seasonchampion;

import com.f1.seasonchampions.dto.SeasonChampionListItem;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonChampionQueryService {
  private final SeasonChampionRepository seasonChampionRepository;

  @Cacheable("all-seasons-cache")
  @NotNull
  public List<SeasonChampionListItem> getAllSeasons() {
    return this.seasonChampionRepository.findAllSeasonChampionListItems();
  }
}
