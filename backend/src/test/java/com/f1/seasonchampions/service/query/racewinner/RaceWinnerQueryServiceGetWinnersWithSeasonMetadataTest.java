package com.f1.seasonchampions.service.query.racewinner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.*;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.SeasonStatusService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RaceWinnerQueryServiceGetWinnersWithSeasonMetadataTest {

  @Mock private RaceWinnerRepository raceWinnerRepository;
  @Mock private DriverRepository driverRepository;
  @Mock private ConstructorRepository constructorRepository;
  @Mock private SeasonStatusService seasonStatusService;

  @InjectMocks private RaceWinnerQueryService raceWinnerQueryService;

  private DriverInfo testDriver;
  private RaceWinnerListItem testWinner;

  @BeforeEach
  void setUp() {
    testDriver = new DriverInfoImpl("Max", "Verstappen", "max_verstappen");
    testWinner =
        new RaceWinnerListItemImpl(
            "2023",
            "1",
            true,
            testDriver,
            "max_verstappen",
            "red_bull",
            "Red Bull Racing",
            "1:28.456");
  }

  @Test
  void whenSeasonConcludedWithChampion_thenReturnCorrectMetadata() {
    // Arrange
    int year = 2023;
    String season = "2023";
    List<RaceWinnerListItem> mockWinners = List.of(testWinner);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(mockWinners);
    when(seasonStatusService.isSeasonConcluded(season, 1)).thenReturn(true);

    // Act
    RaceWinnerSeasonResponse result = raceWinnerQueryService.getWinnersWithSeasonMetadata(year);

    // Assert
    assertNotNull(result);
    assertEquals("2023", result.getSeason());
    assertTrue(result.isSeasonConcluded());
    assertTrue(result.isHasChampion());
    assertEquals(1, result.getTotalRaces());
    assertEquals(1, result.getRaceWinners().size());
    assertEquals("max_verstappen", result.getRaceWinners().get(0).getSeasonDriverId());
    assertTrue(result.getRaceWinners().get(0).isChampion());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 1);
  }

  @Test
  void whenSeasonConcludedWithoutChampion_thenReturnCorrectMetadata() {
    // Arrange
    int year = 2022;
    String season = "2022";
    RaceWinnerListItem winnerWithoutChampion =
        new RaceWinnerListItemImpl(
            "2022",
            "1",
            false,
            testDriver,
            "max_verstappen",
            "red_bull",
            "Red Bull Racing",
            "1:28.456");
    List<RaceWinnerListItem> mockWinners = List.of(winnerWithoutChampion);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(mockWinners);
    when(seasonStatusService.isSeasonConcluded(season, 1)).thenReturn(true);

    // Act
    RaceWinnerSeasonResponse result = raceWinnerQueryService.getWinnersWithSeasonMetadata(year);

    // Assert
    assertNotNull(result);
    assertEquals("2022", result.getSeason());
    assertTrue(result.isSeasonConcluded());
    assertFalse(result.isHasChampion());
    assertEquals(1, result.getTotalRaces());
    assertEquals(1, result.getRaceWinners().size());
    assertFalse(result.getRaceWinners().get(0).isChampion());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 1);
  }

  @Test
  void whenSeasonOngoingWithChampionData_thenClearChampionStatusAndReturnCorrectMetadata() {
    // Arrange
    int year = 2024;
    String season = "2024";
    List<RaceWinnerListItem> mockWinnersWithChampion = List.of(testWinner);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season))
        .thenReturn(mockWinnersWithChampion);
    when(seasonStatusService.isSeasonConcluded(season, 1)).thenReturn(false);

    // Act
    RaceWinnerSeasonResponse result = raceWinnerQueryService.getWinnersWithSeasonMetadata(year);

    // Assert
    assertNotNull(result);
    assertEquals("2024", result.getSeason());
    assertFalse(result.isSeasonConcluded());
    assertFalse(result.isHasChampion()); // Champion status should be cleared for ongoing season
    assertEquals(1, result.getTotalRaces());
    assertEquals(1, result.getRaceWinners().size());
    assertFalse(result.getRaceWinners().get(0).isChampion()); // Champion status cleared

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 1);
  }

  @Test
  void whenMultipleWinnersInOngoingSeason_thenClearAllChampionStatus() {
    // Arrange
    int year = 2024;
    String season = "2024";

    DriverInfo driver2 = new DriverInfoImpl("Lewis", "Hamilton", "lewis_hamilton");
    DriverInfo driver3 = new DriverInfoImpl("Charles", "Leclerc", "charles_leclerc");

    RaceWinnerListItem winner1 =
        new RaceWinnerListItemImpl(
            "2024",
            "1",
            true,
            testDriver,
            "max_verstappen",
            "red_bull",
            "Red Bull Racing",
            "1:28.456");
    RaceWinnerListItem winner2 =
        new RaceWinnerListItemImpl(
            "2024", "2", true, driver2, "lewis_hamilton", "mercedes", "Mercedes", "1:29.123");
    RaceWinnerListItem winner3 =
        new RaceWinnerListItemImpl(
            "2024", "3", false, driver3, "charles_leclerc", "ferrari", "Ferrari", "1:30.789");

    List<RaceWinnerListItem> mockWinners = List.of(winner1, winner2, winner3);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(mockWinners);
    when(seasonStatusService.isSeasonConcluded(season, 3)).thenReturn(false);

    // Act
    RaceWinnerSeasonResponse result = raceWinnerQueryService.getWinnersWithSeasonMetadata(year);

    // Assert
    assertNotNull(result);
    assertEquals("2024", result.getSeason());
    assertFalse(result.isSeasonConcluded());
    assertFalse(result.isHasChampion());
    assertEquals(3, result.getTotalRaces());
    assertEquals(3, result.getRaceWinners().size());

    // Verify all champion statuses are cleared
    result
        .getRaceWinners()
        .forEach(
            winner ->
                assertFalse(
                    winner.isChampion(), "Champion status should be cleared for ongoing season"));

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 3);
  }

  @Test
  void whenEmptySeasonData_thenReturnEmptyMetadata() {
    // Arrange
    int year = 2025;
    String season = "2025";
    List<RaceWinnerListItem> emptyWinners = List.of();

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(emptyWinners);
    when(seasonStatusService.isSeasonConcluded(season, 0)).thenReturn(false);

    // Act
    RaceWinnerSeasonResponse result = raceWinnerQueryService.getWinnersWithSeasonMetadata(year);

    // Assert
    assertNotNull(result);
    assertEquals("2025", result.getSeason());
    assertFalse(result.isSeasonConcluded());
    assertFalse(result.isHasChampion());
    assertEquals(0, result.getTotalRaces());
    assertTrue(result.getRaceWinners().isEmpty());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 0);
  }

  @Test
  void whenConcludedSeasonWithMultipleChampions_thenPreserveChampionStatus() {
    // Arrange
    int year = 2021;
    String season = "2021";

    DriverInfo driver2 = new DriverInfoImpl("Lewis", "Hamilton", "lewis_hamilton");

    RaceWinnerListItem championWinner =
        new RaceWinnerListItemImpl(
            "2021",
            "1",
            true,
            testDriver,
            "max_verstappen",
            "red_bull",
            "Red Bull Racing",
            "1:28.456");
    RaceWinnerListItem nonChampionWinner =
        new RaceWinnerListItemImpl(
            "2021", "2", false, driver2, "lewis_hamilton", "mercedes", "Mercedes", "1:29.123");

    List<RaceWinnerListItem> mockWinners = List.of(championWinner, nonChampionWinner);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(mockWinners);
    when(seasonStatusService.isSeasonConcluded(season, 2)).thenReturn(true);

    // Act
    RaceWinnerSeasonResponse result = raceWinnerQueryService.getWinnersWithSeasonMetadata(year);

    // Assert
    assertNotNull(result);
    assertEquals("2021", result.getSeason());
    assertTrue(result.isSeasonConcluded());
    assertTrue(result.isHasChampion());
    assertEquals(2, result.getTotalRaces());
    assertEquals(2, result.getRaceWinners().size());

    // Verify champion status is preserved
    assertTrue(result.getRaceWinners().get(0).isChampion());
    assertFalse(result.getRaceWinners().get(1).isChampion());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 2);
  }

  @Test
  void whenRepositoryThrowsException_thenPropagateException() {
    // Arrange
    int year = 2023;
    String season = "2023";

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season))
        .thenThrow(new RuntimeException("Database connection failed"));

    // Act & Assert
    assertThrows(
        RuntimeException.class, () -> raceWinnerQueryService.getWinnersWithSeasonMetadata(year));

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService, never()).isSeasonConcluded(anyString(), anyInt());
  }

  @Test
  void whenSeasonStatusServiceThrowsException_thenPropagateException() {
    // Arrange
    int year = 2023;
    String season = "2023";
    List<RaceWinnerListItem> mockWinners = List.of(testWinner);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(mockWinners);
    when(seasonStatusService.isSeasonConcluded(season, 1))
        .thenThrow(new RuntimeException("Season status check failed"));

    // Act & Assert
    assertThrows(
        RuntimeException.class, () -> raceWinnerQueryService.getWinnersWithSeasonMetadata(year));

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 1);
  }

  @Test
  void whenRaceWinnerDataIntegrity_thenReturnCorrectDriverAndConstructorInfo() {
    // Arrange
    int year = 2023;
    String season = "2023";

    DriverInfo complexDriver = new DriverInfoImpl("Fernando", "Alonso", "fernando_alonso");
    RaceWinnerListItem complexWinner =
        new RaceWinnerListItemImpl(
            "2023",
            "5",
            true,
            complexDriver,
            "fernando_alonso",
            "aston_martin",
            "Aston Martin Aramco",
            "1:32.789");

    List<RaceWinnerListItem> mockWinners = List.of(complexWinner);

    when(raceWinnerRepository.findRaceWinnersWithConstructors(season)).thenReturn(mockWinners);
    when(seasonStatusService.isSeasonConcluded(season, 1)).thenReturn(true);

    // Act
    RaceWinnerSeasonResponse result = raceWinnerQueryService.getWinnersWithSeasonMetadata(year);

    // Assert
    assertNotNull(result);
    RaceWinnerListItem resultWinner = result.getRaceWinners().get(0);

    assertEquals("2023", resultWinner.getSeasonName());
    assertEquals("5", resultWinner.getRound());
    assertTrue(resultWinner.isChampion());
    assertEquals("Fernando", resultWinner.getDriver().getGivenName());
    assertEquals("Alonso", resultWinner.getDriver().getFamilyName());
    assertEquals("fernando_alonso", resultWinner.getDriver().getDriverId());
    assertEquals("fernando_alonso", resultWinner.getSeasonDriverId());
    assertEquals("aston_martin", resultWinner.getSeasonConstructorId());
    assertEquals("Aston Martin Aramco", resultWinner.getConstructorName());
    assertEquals("1:32.789", resultWinner.getTime());

    verify(raceWinnerRepository).findRaceWinnersWithConstructors(season);
    verify(seasonStatusService).isSeasonConcluded(season, 1);
  }
}
