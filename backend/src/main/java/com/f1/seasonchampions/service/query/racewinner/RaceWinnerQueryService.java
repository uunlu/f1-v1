package com.f1.seasonchampions.service.query.racewinner;

import com.f1.seasonchampions.dto.RaceWinnerListItem;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceWinnerQueryService {
  @NotNull
  public List<RaceWinnerListItem> getWinnersBySeason(int year) {
    return List.of();
  }
}
