package com.f1.seasonchampions.service.racewinner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.scheduler.F1DataSchedulerService;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class F1DataSchedulerServiceTest {

  @Mock private RaceWinnerSeedService raceWinnerSeedService;

  @Mock private RaceWinnerRepository raceWinnerRepository;

  @InjectMocks private F1DataSchedulerService f1DataSchedulerService;

  private RaceWinner mockWinner(int round) {
    RaceWinner winner = new RaceWinner();
    winner.setSeason("2025");
    winner.setRound(String.valueOf(round));
    return winner;
  }

  @BeforeEach
  void setUp() {
    // No-op for now
  }

  @Test
  void whenNewRaceWinnerAvailable_thenItIsSaved() {
    // Arrange
    int currentYear = 2025;
    when(raceWinnerRepository.getRaceWinnerBySeason(String.valueOf(currentYear)))
        .thenReturn(List.of(mockWinner(1), mockWinner(2)));

    List<RaceWinner> apiResults = List.of(mockWinner(1), mockWinner(2), mockWinner(3));
    when(raceWinnerSeedService.getRaceWinners(currentYear)).thenReturn(apiResults);

    // Act
    f1DataSchedulerService.syncLatestF1RaceResult();

    // Assert
    verify(raceWinnerRepository, times(1)).save(argThat(w -> w.getRound().equals("3")));
  }

  @Test
  void whenNoNewData_thenNothingIsSaved() {
    int currentYear = 2025;
    when(raceWinnerRepository.getRaceWinnerBySeason(String.valueOf(currentYear)))
        .thenReturn(List.of(mockWinner(1), mockWinner(2), mockWinner(3)));

    List<RaceWinner> apiResults = List.of(mockWinner(1), mockWinner(2), mockWinner(3));
    when(raceWinnerSeedService.getRaceWinners(currentYear)).thenReturn(apiResults);

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenApiReturnsNoResults_thenExitEarly() {
    when(raceWinnerSeedService.getRaceWinners(anyInt())).thenReturn(List.of());

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenRepositoryReturnsEmpty_thenAllResultsAreSaved() {
    int currentYear = 2025;
    when(raceWinnerRepository.getRaceWinnerBySeason(String.valueOf(currentYear)))
        .thenReturn(List.of());

    List<RaceWinner> winners = List.of(mockWinner(1), mockWinner(2));
    when(raceWinnerSeedService.getRaceWinners(currentYear)).thenReturn(winners);

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerRepository, times(2)).save(any());
  }

  @Test
  void whenExceptionThrown_thenItIsHandledGracefully() {
    when(raceWinnerSeedService.getRaceWinners(anyInt()))
        .thenThrow(new RuntimeException("Simulated API error"));

    assertDoesNotThrow(() -> f1DataSchedulerService.syncLatestF1RaceResult());
  }
}
