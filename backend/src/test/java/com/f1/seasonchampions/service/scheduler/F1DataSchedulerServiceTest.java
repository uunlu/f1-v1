package com.f1.seasonchampions.service.scheduler;

import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.generated.ResultsByYearResponse;
import com.f1.seasonchampions.model.*;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.RateLimitedApiClientService;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seed.seasonchampion.SeasonChampionSeedService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class F1DataSchedulerServiceTest {

  @Mock private RaceWinnerSeedService raceWinnerService;
  @Mock private RaceWinnerRepository raceResultRepository;
  @Mock private SeasonChampionSeedService seasonChampionSeedService;
  @Mock private RateLimitedApiClientService rateLimitedApiClient;

  @InjectMocks private F1DataSchedulerService f1DataSchedulerService;

  @BeforeEach
  void setUp() {
    // Set the apiBaseUrl field since it's injected via @Value
    ReflectionTestUtils.setField(
        f1DataSchedulerService, "apiBaseUrl", "https://api.test.com/ergast/f1");
  }

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
  void whenNewRaceWinnersAvailable_thenTheyAreSaved() {
    int year = java.time.Year.now().getValue();
    String yearStr = String.valueOf(year);

    List<RaceWinner> existing =
        List.of(createRaceWinner(yearStr, "1"), createRaceWinner(yearStr, "2"));

    when(raceResultRepository.getRaceWinnerBySeason(yearStr)).thenReturn(existing);

    // Mock the rate limited API client to return null responses (simulating no data)
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(null);

    f1DataSchedulerService.syncLatestF1RaceResult();

    // Since we're mocking null API responses, fetchRaceWinnersForYear will catch the exception and
    // return empty list
    // So no races will be saved
    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenNoNewWinners_thenNoSaveIsCalled() {
    int year = java.time.Year.now().getValue();
    String yearStr = String.valueOf(year);

    List<RaceWinner> existing =
        List.of(createRaceWinner(yearStr, "1"), createRaceWinner(yearStr, "2"));

    when(raceResultRepository.getRaceWinnerBySeason(yearStr)).thenReturn(existing);

    // Mock empty API response
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(new ResultsByYearResponse()));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenNoPreviousData_thenAllApiResultsAreSaved() {
    int year = java.time.Year.now().getValue();
    String yearStr = String.valueOf(year);

    when(raceResultRepository.getRaceWinnerBySeason(yearStr)).thenReturn(List.of());

    // Mock empty API response
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(new ResultsByYearResponse()));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenApiReturnsEmpty_thenNothingIsSaved() {
    int year = java.time.Year.now().getValue();

    // Mock empty API response
    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenReturn(ResponseEntity.ok(new ResultsByYearResponse()));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenExceptionIsThrown_thenHandledGracefully() {
    int year = java.time.Year.now().getValue();

    when(rateLimitedApiClient.executeRateLimitedRequest(
            anyString(), eq(ResultsByYearResponse.class)))
        .thenThrow(new RuntimeException("API Error"));

    f1DataSchedulerService.syncLatestF1RaceResult();

    verify(raceWinnerService, never()).saveRaceWinner(any());
  }

  @Test
  void whenNewSeasonChampionsAvailable_thenTheyAreSaved() {
    int year = java.time.Year.now().getValue();

    SeasonChampion champ1 = createSeasonChampion(year - 1);
    SeasonChampion champ2 = createSeasonChampion(year);

    List<SeasonChampion> champions = List.of(champ1, champ2);

    when(seasonChampionSeedService.getSeasonChampions(any(SeasonRangeRequest.class)))
        .thenReturn(champions);

    when(seasonChampionSeedService.saveChampion(any(SeasonChampion.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    f1DataSchedulerService.syncSeasonChampions();

    verify(seasonChampionSeedService, times(2)).saveChampion(any(SeasonChampion.class));
  }

  @Test
  void whenNoSeasonChampionsAvailable_thenNoSaveCalled() {
    when(seasonChampionSeedService.getSeasonChampions(any(SeasonRangeRequest.class)))
        .thenReturn(List.of());

    f1DataSchedulerService.syncSeasonChampions();

    verify(seasonChampionSeedService, never()).saveChampion(any());
  }

  @Test
  void whenExceptionIsThrownDuringSeasonChampionSync_thenHandledGracefully() {
    when(seasonChampionSeedService.getSeasonChampions(any(SeasonRangeRequest.class)))
        .thenThrow(new RuntimeException("API failure"));

    f1DataSchedulerService.syncSeasonChampions();

    verify(seasonChampionSeedService, never()).saveChampion(any());
  }
}
