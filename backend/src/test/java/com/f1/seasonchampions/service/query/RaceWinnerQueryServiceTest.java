package com.f1.seasonchampions.service.query.racewinner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.DriverInfo;
import com.f1.seasonchampions.dto.DriverInfoImpl;
import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.dto.RaceWinnerListItemImpl;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RaceWinnerQueryServiceTest {

  @Mock private RaceWinnerRepository raceWinnerRepository;
  @Mock private DriverRepository driverRepository;
  @Mock private ConstructorRepository constructorRepository;

  @InjectMocks private RaceWinnerQueryService raceWinnerQueryService;

  private RaceWinnerListItemImpl createRaceWinner(
      String season,
      String round,
      boolean champion,
      String driverId,
      String driverCode,
      String driverName,
      String constructorName) {

    DriverInfo driverInfo = new DriverInfoImpl(driverId, driverCode, driverName);

    return new RaceWinnerListItemImpl(
        season,
        round,
        champion,
        driverInfo,
        driverId,
        "constructor_id_" + constructorName.toLowerCase().replace(" ", "_"),
        constructorName);
  }

  @Test
  void whenRaceWinnersExist_thenReturnList() {
    int year = 2023;
    String season = String.valueOf(year);

    RaceWinnerListItemImpl winner1 =
        createRaceWinner(season, "1", false, "max_verstappen", "VER", "Max Verstappen", "Red Bull");
    RaceWinnerListItemImpl winner2 =
        createRaceWinner(season, "2", true, "lewis_hamilton", "HAM", "Lewis Hamilton", "Mercedes");

    List<RaceWinnerListItem> expected = List.of(winner1, winner2);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(expected);

    List<RaceWinnerListItem> result = raceWinnerQueryService.getWinnersBySeason(year);

    assertEquals(2, result.size());

    RaceWinnerListItem first = result.get(0);
    assertEquals("max_verstappen", first.getSeasonDriverId());
    assertFalse(first.isChampion());
    assertEquals("Red Bull", first.getConstructorName());

    RaceWinnerListItem second = result.get(1);
    assertEquals("lewis_hamilton", second.getSeasonDriverId());
    assertTrue(second.isChampion());
    assertEquals("Mercedes", second.getConstructorName());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
  }

  @Test
  void whenNoRaceWinnersExist_thenReturnEmptyList() {
    int year = 2022;

    when(raceWinnerRepository.findRaceWinnersWithConstructors(String.valueOf(year)))
        .thenReturn(List.of());

    List<RaceWinnerListItem> result = raceWinnerQueryService.getWinnersBySeason(year);

    assertTrue(result.isEmpty());
    verify(raceWinnerRepository).findRaceWinnersWithConstructors(String.valueOf(year));
  }
}
