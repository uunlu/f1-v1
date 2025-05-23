package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.exception.InvalidInputException;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
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
class LocalWithRemoteFallbackSeasonChampionServiceTest {

  @Mock private LocalSeasonChampionService localService;

  @Mock private RemoteSeasonChampionService remoteService;

  @InjectMocks private LocalWithRemoteFallbackSeasonChampionService service;

  private SeasonChampion champion2021;
  private SeasonChampion champion2022;

  @BeforeEach
  void setUp() {
    Driver driver2021 =
        Driver.builder()
            .driverId("max_verstappen")
            .givenName("Max")
            .familyName("Verstappen")
            .nationality("Dutch")
            .code("VER")
            .build();

    Constructor constructor2021 =
        Constructor.builder()
            .constructorId("red_bull")
            .name("Red Bull Racing")
            .nationality("Austrian")
            .build();

    champion2021 =
        SeasonChampion.builder()
            .season("2021")
            .driver(driver2021)
            .constructor(constructor2021)
            .build();

    Driver driver2022 =
        Driver.builder()
            .driverId("max_verstappen")
            .givenName("Max")
            .familyName("Verstappen")
            .nationality("Dutch")
            .code("VER")
            .build();

    Constructor constructor2022 =
        Constructor.builder()
            .constructorId("red_bull")
            .name("Red Bull Racing")
            .nationality("Austrian")
            .build();

    champion2022 =
        SeasonChampion.builder()
            .season("2022")
            .driver(driver2022)
            .constructor(constructor2022)
            .build();
  }

  @Test
  void whenStartYearGreaterThanEndYear_thenThrowException() {
    SeasonRangeRequest request = new SeasonRangeRequest(2022, 2021);

    assertThrows(InvalidInputException.class, () -> service.getSeasonChampions(request));
  }

  @Test
  void whenLocalServiceHasCompleteData_thenReturnLocalData() {
    SeasonRangeRequest request = new SeasonRangeRequest(2021, 2022);
    List<SeasonChampion> expectedChampions = Arrays.asList(champion2021, champion2022);

    when(localService.hasCompleteDataForRange(request)).thenReturn(true);
    when(localService.getSeasonChampions(request)).thenReturn(expectedChampions);

    List<SeasonChampion> result = service.getSeasonChampions(request);

    assertEquals(expectedChampions, result);
    verify(remoteService, never()).getSeasonChampions(any());
  }

  @Test
  void whenLocalServiceHasPartialData_thenFetchRemaining() {
    SeasonRangeRequest request = new SeasonRangeRequest(2021, 2022);

    when(localService.hasCompleteDataForRange(request)).thenReturn(false);
    when(localService.getSeasonChampions(request))
        .thenReturn(Collections.singletonList(champion2021));
    when(remoteService.getSeasonChampions(new SeasonRangeRequest(2022, 2022)))
        .thenReturn(Collections.singletonList(champion2022));
    when(localService.saveChampion(champion2022)).thenReturn(champion2022);

    List<SeasonChampion> result = service.getSeasonChampions(request);

    assertEquals(2, result.size());
    assertEquals("2021", result.get(0).getSeason());
    assertEquals("2022", result.get(1).getSeason());
    verify(remoteService, times(1)).getSeasonChampions(any());
    verify(localService, times(1)).saveChampion(any());
  }

  @Test
  void whenSavingChampion_thenDelegateToLocalService() {
    when(localService.saveChampion(champion2021)).thenReturn(champion2021);

    SeasonChampion result = service.saveChampion(champion2021);

    assertEquals(champion2021, result);
    verify(localService, times(1)).saveChampion(champion2021);
  }
}
