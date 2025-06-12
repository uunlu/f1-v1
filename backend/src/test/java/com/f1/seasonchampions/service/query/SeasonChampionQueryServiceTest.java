package com.f1.seasonchampions.service.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.f1.seasonchampions.dto.SeasonChampionListItem;
import com.f1.seasonchampions.dto.SeasonChampionListItemImpl;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import com.f1.seasonchampions.service.SeasonStatusService;
import com.f1.seasonchampions.service.query.seasonchampion.SeasonChampionQueryService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeasonChampionQueryServiceTest {

  @Mock private SeasonChampionRepository seasonChampionRepository;
  @Mock private RaceWinnerRepository raceWinnerRepository;
  @Mock private SeasonStatusService seasonStatusService;

  @InjectMocks private SeasonChampionQueryService seasonChampionQueryService;

  @Test
  void getAllSeasons_shouldReturnSeasonChampionListItemsWithCompletionStatus() {
    // Arrange
    SeasonChampionListItem item1 =
        new SeasonChampionListItemImpl("2020", "Lewis Hamilton", "Mercedes");
    SeasonChampionListItem item2 =
        new SeasonChampionListItemImpl("2021", "Max Verstappen", "Red Bull");
    var mockList = List.of(item1, item2);

    // Mock race winners (simulate completed seasons)
    List<RaceWinner> raceWinners2020 = Collections.nCopies(23, new RaceWinner()); // Full season
    List<RaceWinner> raceWinners2021 =
        Collections.nCopies(22, new RaceWinner()); // Almost full season

    when(seasonChampionRepository.findAllSeasonChampionListItems()).thenReturn(mockList);
    when(raceWinnerRepository.getRaceWinnerBySeason("2020")).thenReturn(raceWinners2020);
    when(raceWinnerRepository.getRaceWinnerBySeason("2021")).thenReturn(raceWinners2021);
    when(seasonStatusService.isSeasonConcluded(eq("2020"), eq(23))).thenReturn(true);
    when(seasonStatusService.isSeasonConcluded(eq("2021"), eq(22))).thenReturn(true);

    // Act
    var result = seasonChampionQueryService.getAllSeasons();

    // Assert
    assertEquals(2, result.size());

    // Verify 2020 season data
    assertEquals("2020", result.get(0).getSeason());
    assertEquals("Lewis Hamilton", result.get(0).getDriver());
    assertEquals("Mercedes", result.get(0).getConstructor());
    assertTrue(result.get(0).isCompleted());

    // Verify 2021 season data
    assertEquals("2021", result.get(1).getSeason());
    assertEquals("Max Verstappen", result.get(1).getDriver());
    assertEquals("Red Bull", result.get(1).getConstructor());
    assertTrue(result.get(1).isCompleted());

    verify(seasonChampionRepository).findAllSeasonChampionListItems();
    verify(raceWinnerRepository).getRaceWinnerBySeason("2020");
    verify(raceWinnerRepository).getRaceWinnerBySeason("2021");
    verify(seasonStatusService).isSeasonConcluded("2020", 23);
    verify(seasonStatusService).isSeasonConcluded("2021", 22);
  }

  @Test
  void getAllSeasons_withOngoingSeason_shouldReturnNotCompleted() {
    // Arrange
    SeasonChampionListItem item1 =
        new SeasonChampionListItemImpl("2024", "Max Verstappen", "Red Bull");
    var mockList = List.of(item1);

    // Mock ongoing season with only 10 races completed
    List<RaceWinner> raceWinners2024 = Collections.nCopies(10, new RaceWinner());

    when(seasonChampionRepository.findAllSeasonChampionListItems()).thenReturn(mockList);
    when(raceWinnerRepository.getRaceWinnerBySeason("2024")).thenReturn(raceWinners2024);
    when(seasonStatusService.isSeasonConcluded(eq("2024"), eq(10))).thenReturn(false);

    // Act
    var result = seasonChampionQueryService.getAllSeasons();

    // Assert
    assertEquals(1, result.size());
    assertEquals("2024", result.get(0).getSeason());
    assertEquals("Max Verstappen", result.get(0).getDriver());
    assertEquals("Red Bull", result.get(0).getConstructor());
    assertFalse(result.get(0).isCompleted());

    verify(seasonStatusService).isSeasonConcluded("2024", 10);
  }

  @Test
  void getAllSeasons_whenExceptionOccurs_shouldReturnNotCompleted() {
    // Arrange
    SeasonChampionListItem item1 =
        new SeasonChampionListItemImpl("2023", "Max Verstappen", "Red Bull");
    var mockList = List.of(item1);

    when(seasonChampionRepository.findAllSeasonChampionListItems()).thenReturn(mockList);
    when(raceWinnerRepository.getRaceWinnerBySeason("2023"))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    var result = seasonChampionQueryService.getAllSeasons();

    // Assert
    assertEquals(1, result.size());
    assertEquals("2023", result.get(0).getSeason());
    assertFalse(result.get(0).isCompleted()); // Should default to false on error
  }
}
