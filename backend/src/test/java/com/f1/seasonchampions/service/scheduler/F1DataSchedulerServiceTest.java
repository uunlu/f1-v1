package com.f1.seasonchampions.service.scheduler;

import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.*;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.F1RaceDataFetchingService;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seed.seasonchampion.SeasonChampionSeedService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class F1DataSchedulerServiceTest {

  @Mock private RaceWinnerSeedService raceWinnerService;
  @Mock private RaceWinnerRepository raceResultRepository;
  @Mock private SeasonChampionSeedService seasonChampionSeedService;
  @Mock private F1RaceDataFetchingService f1RaceDataFetchingService;

  @InjectMocks private F1DataSchedulerService f1DataSchedulerService;

  private RaceWinner createRaceWinner(String season, String round) {
    RaceWinner winner = new RaceWinner();
    winner.setSeason(season);
    winner.setRound(round);

    Driver driver = new Driver();
    driver.setDriverId("max_verstappen");
    driver.setGivenName("Max");
    driver.setFamilyName("Verstappen");
    driver.setCode("VER");
    winner.setDriver(driver);

    Constructor constructor = new Constructor();
    constructor.setConstructorId("red_bull");
    constructor.setName("Red Bull Racing");
    winner.setConstructor(constructor);

    return winner;
  }

  private SeasonChampion createSeasonChampion(int year) {
    final SeasonChampion champion = new SeasonChampion();
    champion.setSeason(String.valueOf(year));
    final var driver = new Driver();
    driver.setDriverId("lewis_hamilton");
    final var constructor = new Constructor();
    constructor.setConstructorId("mercedes");
    champion.setDriver(driver);
    champion.setConstructor(constructor);
    return champion;
  }

  @Test
  void whenRaceWinnersAvailable_thenTheyAreSaved() {
    int year = java.time.Year.now().getValue();

    List<RaceWinner> newWinners =
        List.of(
            createRaceWinner(String.valueOf(year), "1"),
            createRaceWinner(String.valueOf(year), "2"));

    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(year)).thenReturn(newWinners);
    when(raceWinnerService.saveRaceWinner(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, times(2)).saveRaceWinner(any());
  }

  @Test
  void whenNoRaceWinners_thenNoSaveIsCalled() {
    int year = java.time.Year.now().getValue();

    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(year)).thenReturn(List.of());

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenApiReturnsEmpty_thenNothingIsSaved() {
    int year = java.time.Year.now().getValue();

    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(year)).thenReturn(List.of());

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenExceptionIsThrown_thenHandledGracefully() {
    int year = java.time.Year.now().getValue();

    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(year))
        .thenThrow(new RuntimeException("API Error"));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenSeasonChampionsAvailable_thenTheyAreRetrieved() {
    int year = java.time.Year.now().getValue();

    SeasonChampion champ1 = createSeasonChampion(year - 1);
    SeasonChampion champ2 = createSeasonChampion(year);

    List<SeasonChampion> champions = List.of(champ1, champ2);

    when(seasonChampionSeedService.getSeasonChampions(any(SeasonRangeRequest.class)))
        .thenReturn(champions);

    f1DataSchedulerService.syncSeasonChampions();

    // The service only retrieves champions, it doesn't save them
    verify(seasonChampionSeedService, times(1)).getSeasonChampions(any(SeasonRangeRequest.class));
    verify(seasonChampionSeedService, never()).saveChampion(any());
  }

  @Test
  void whenNoSeasonChampionsAvailable_thenNoSaveCalled() {
    when(seasonChampionSeedService.getSeasonChampions(any(SeasonRangeRequest.class)))
        .thenReturn(List.of());

    f1DataSchedulerService.syncSeasonChampions();

    verify(seasonChampionSeedService, times(1)).getSeasonChampions(any(SeasonRangeRequest.class));
    verify(seasonChampionSeedService, never()).saveChampion(any());
  }

  @Test
  void whenExceptionIsThrownDuringSeasonChampionSync_thenHandledGracefully() {
    when(seasonChampionSeedService.getSeasonChampions(any(SeasonRangeRequest.class)))
        .thenThrow(new RuntimeException("API failure"));

    f1DataSchedulerService.syncSeasonChampions();

    verify(seasonChampionSeedService, times(1)).getSeasonChampions(any(SeasonRangeRequest.class));
    verify(seasonChampionSeedService, never()).saveChampion(any());
  }

  @Test
  void whenLastProcessedRoundExists_thenItIsRetrieved() {
    int year = java.time.Year.now().getValue();
    String yearStr = String.valueOf(year);

    when(raceResultRepository.findMaxRoundByYear(yearStr)).thenReturn(Optional.of(5));
    when(f1RaceDataFetchingService.fetchRaceWinnersForYear(year)).thenReturn(List.of());

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceResultRepository).findMaxRoundByYear(yearStr);
  }
}
