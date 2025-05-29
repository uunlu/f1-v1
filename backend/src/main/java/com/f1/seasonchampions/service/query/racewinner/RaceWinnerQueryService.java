package com.f1.seasonchampions.service.query.racewinner;

import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RaceWinnerQueryService {
  private final RaceWinnerRepository raceWinnerRepository;
  private final DriverRepository driverRepository;
  private final ConstructorRepository constructorRepository;

  @NotNull
  public List<RaceWinnerListItem> getWinnersBySeason(final int year) {
    return this.raceWinnerRepository.findBySeasonAndOptionalRound2(String.valueOf(year));
  }
}
