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
import com.f1.seasonchampions.service.SeasonStatusService;
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
  @Mock private SeasonStatusService seasonStatusService;

  @InjectMocks private RaceWinnerQueryService raceWinnerQueryService;

  private RaceWinnerListItemImpl createRaceWinner(
      String season,
      String round,
      boolean champion,
      String driverId,
      String driverCode,
      String driverName,
      String constructorName,
      String time) {

    DriverInfo driverInfo = new DriverInfoImpl(driverId, driverCode, driverName);

    return new RaceWinnerListItemImpl(
        season,
        round,
        champion,
        driverInfo,
        driverId,
        "constructor_id_" + constructorName.toLowerCase().replace(" ", "_"),
        constructorName,
        time);
  }

  @Test
  void whenRaceWinnersExistAndSeasonConcluded_thenReturnListWithChampionStatus() {
    // Arrange
    int year = 2022;
    String season = String.valueOf(year);

    RaceWinnerListItemImpl winner1 =
        createRaceWinner(
            season,
            "1",
            false,
            "max_verstappen",
            "VER",
            "Max Verstappen",
            "Red Bull",
            "1:37:33.584");
    RaceWinnerListItemImpl winner2 =
        createRaceWinner(
            season,
            "2",
            true,
            "lewis_hamilton",
            "HAM",
            "Lewis Hamilton",
            "Mercedes",
            "1:37:33.584");

    List<RaceWinnerListItem> expected = List.of(winner1, winner2);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(expected);
    when(seasonStatusService.isSeasonConcluded(season, 2)).thenReturn(true);

    // Act
    List<RaceWinnerListItem> result = raceWinnerQueryService.getWinnersBySeason(year);

    // Assert
    assertEquals(2, result.size());

    RaceWinnerListItem first = result.get(0);
    assertEquals("max_verstappen", first.getSeasonDriverId());
    assertFalse(first.isChampion());
    assertEquals("Red Bull", first.getConstructorName());

    RaceWinnerListItem second = result.get(1);
    assertEquals("lewis_hamilton", second.getSeasonDriverId());
    assertTrue(second.isChampion()); // Champion status preserved for concluded season
    assertEquals("Mercedes", second.getConstructorName());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 2);
  }

  @Test
  void whenRaceWinnersExistAndSeasonOngoing_thenReturnListWithoutChampionStatus() {
    // Arrange
    int year = 2024;
    String season = String.valueOf(year);

    RaceWinnerListItemImpl winner1 =
        createRaceWinner(
            season,
            "1",
            false,
            "max_verstappen",
            "VER",
            "Max Verstappen",
            "Red Bull",
            "1:37:33.584");
    RaceWinnerListItemImpl winner2 =
        createRaceWinner(
            season,
            "2",
            true,
            "lewis_hamilton",
            "HAM",
            "Lewis Hamilton",
            "Mercedes",
            "1:37:33.584");

    List<RaceWinnerListItem> originalWinners = List.of(winner1, winner2);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(originalWinners);
    when(seasonStatusService.isSeasonConcluded(season, 2)).thenReturn(false);

    // Act
    List<RaceWinnerListItem> result = raceWinnerQueryService.getWinnersBySeason(year);

    // Assert
    assertEquals(2, result.size());

    RaceWinnerListItem first = result.get(0);
    assertEquals("max_verstappen", first.getSeasonDriverId());
    assertFalse(first.isChampion());
    assertEquals("Red Bull", first.getConstructorName());

    RaceWinnerListItem second = result.get(1);
    assertEquals("lewis_hamilton", second.getSeasonDriverId());
    assertFalse(second.isChampion()); // Champion status cleared for ongoing season
    assertEquals("Mercedes", second.getConstructorName());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 2);
  }

  @Test
  void whenNoRaceWinnersExist_thenReturnEmptyList() {
    // Arrange
    int year = 2022;
    String season = String.valueOf(year);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(List.of());
    when(seasonStatusService.isSeasonConcluded(season, 0)).thenReturn(true);

    // Act
    List<RaceWinnerListItem> result = raceWinnerQueryService.getWinnersBySeason(year);

    // Assert
    assertTrue(result.isEmpty());
    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 0);
  }

  @Test
  void whenOngoingSeasonWithMultipleChampions_thenClearAllChampionStatus() {
    // Arrange
    int year = 2024;
    String season = String.valueOf(year);

    RaceWinnerListItemImpl winner1 =
        createRaceWinner(
            season,
            "1",
            true,
            "max_verstappen",
            "VER",
            "Max Verstappen",
            "Red Bull",
            "1:37:33.584");
    RaceWinnerListItemImpl winner2 =
        createRaceWinner(
            season,
            "2",
            true,
            "charles_leclerc",
            "LEC",
            "Charles Leclerc",
            "Ferrari",
            "1:37:33.584");
    RaceWinnerListItemImpl winner3 =
        createRaceWinner(
            season,
            "3",
            false,
            "lewis_hamilton",
            "HAM",
            "Lewis Hamilton",
            "Mercedes",
            "1:37:33.584");

    List<RaceWinnerListItem> originalWinners = List.of(winner1, winner2, winner3);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(originalWinners);
    when(seasonStatusService.isSeasonConcluded(season, 3)).thenReturn(false);

    // Act
    List<RaceWinnerListItem> result = raceWinnerQueryService.getWinnersBySeason(year);

    // Assert
    assertEquals(3, result.size());

    // All drivers should have champion status cleared
    result.forEach(winner -> assertFalse(winner.isChampion()));

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 3);
  }
}
