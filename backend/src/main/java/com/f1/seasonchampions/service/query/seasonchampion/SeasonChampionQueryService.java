package com.f1.seasonchampions.service.query.seasonchampion;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonChampionQueryService {
  @NotNull
public  List<String> getAllSeasons() {
    return List.of();
  }
}
