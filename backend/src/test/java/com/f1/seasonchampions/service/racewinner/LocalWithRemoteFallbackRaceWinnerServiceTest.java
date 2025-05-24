package com.f1.seasonchampions.service.racewinner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalWithRemoteFallbackRaceWinnerServiceTest {
  @Mock private LocalRaceWinnerService localService;
  @Mock private RemoteRaceWinnerService remoteService;

  @InjectMocks private LocalWithRemoteFallbackRaceWinnerService service;

  private RaceWinner raceWinner2023;
  private Driver driver;
  private Constructor constructor;

  @BeforeEach
  void setUp() {
    driver =
        Driver.builder()
            .driverId("max_verstappen")
            .givenName("Max")
            .familyName("Verstappen")
            .nationality("Dutch")
            .code("VER")
            .build();

    constructor =
        Constructor.builder()
            .constructorId("red_bull")
            .name("Red Bull Racing")
            .nationality("Austrian")
            .build();

    raceWinner2023 = new RaceWinner();
    raceWinner2023.setSeason("2023");
    raceWinner2023.setRound("1");
    raceWinner2023.setDriver(driver);
    raceWinner2023.setConstructor(constructor);
    raceWinner2023.setTime("1:30.000");
  }

  @Test
  void whenLocalServiceHasCompleteData_thenReturnLocalData() {
    when(localService.hasCompleteDataForYear(2023)).thenReturn(true);
    when(localService.getRaceWinners(2023)).thenReturn(Collections.singletonList(raceWinner2023));

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertEquals(1, result.size());
    assertEquals(raceWinner2023, result.get(0));
    verify(remoteService, never()).getRaceWinners(anyInt());
    verify(localService, times(1)).getRaceWinners(2023);
  }

  @Test
  void whenLocalServiceHasNoData_thenFetchFromRemote() {
    when(localService.hasCompleteDataForYear(2023)).thenReturn(false);
    //when(localService.getRaceWinners(2023)).thenReturn(Collections.emptyList());
    when(remoteService.getRaceWinners(2023)).thenReturn(Collections.singletonList(raceWinner2023));
    when(localService.saveRaceWinner(raceWinner2023)).thenReturn(raceWinner2023);

    List<RaceWinner> result = service.getRaceWinners(2023);

    verify(remoteService, times(1)).getRaceWinners(2023);
    verify(localService, times(1)).saveRaceWinner(raceWinner2023);

    assertEquals(1, result.size());
    assertEquals(raceWinner2023, result.getFirst());
  }

  @Test
  void whenLocalServiceHasPartialData_thenFetchRemaining() {
    RaceWinner raceWinner2023Round2 = new RaceWinner();
    raceWinner2023Round2.setSeason("2023");
    raceWinner2023Round2.setRound("2");
    raceWinner2023Round2.setDriver(driver);
    raceWinner2023Round2.setConstructor(constructor);
    raceWinner2023Round2.setTime("1:31.000");

    when(localService.hasCompleteDataForYear(2023)).thenReturn(false);
    when(remoteService.getRaceWinners(2023))
      .thenReturn(Arrays.asList(raceWinner2023, raceWinner2023Round2));

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertEquals(2, result.size());
    assertEquals("1", result.get(0).getRound());
    assertEquals("2", result.get(1).getRound());
    verify(remoteService, times(1)).getRaceWinners(2023);
    verify(localService, times(2)).saveRaceWinner(any(RaceWinner.class));
  }

  @Test
  void whenSavingRaceWinner_thenDelegateToLocalService() {
    when(localService.saveRaceWinner(raceWinner2023)).thenReturn(raceWinner2023);

    RaceWinner result = service.saveRaceWinner(raceWinner2023);

    assertEquals(raceWinner2023, result);
    verify(localService, times(1)).saveRaceWinner(raceWinner2023);
    verifyNoInteractions(remoteService);
  }

  @Test
  void whenCheckingCompleteData_thenDelegateToLocalService() {
    when(localService.hasCompleteDataForYear(2023)).thenReturn(true);

    boolean result = service.hasCompleteDataForYear(2023);

    assertTrue(result);
    verify(localService, times(1)).hasCompleteDataForYear(2023);
    verifyNoInteractions(remoteService);
  }
}
