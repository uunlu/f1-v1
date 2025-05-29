package com.f1.seasonchampions.service.query.seasonchampion;

import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonChampionQueryService {
  @NotNull

  public List<String> getAllSeasons() {
    return List.of();
  }
}
