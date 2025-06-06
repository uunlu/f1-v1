package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.service.seed.seasonchampion.RemoteSeasonChampionSeedService;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemoteSeasonChampionSeedServiceTest {
  @Mock private F1ApiClient f1ApiClient;
  @Mock private RateLimitedApiClientService rateLimitedApiClient;

  @InjectMocks private RemoteSeasonChampionSeedService service;

  private SeasonChampion champion2021;
  private SeasonChampion champion2022;

  @BeforeEach
  void setUp() {
    // Rate limiter is now centralized and automatically initialized

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

    // Mock the rate limited operation to execute the supplier directly
    when(rateLimitedApiClient.executeRateLimitedOperation(any(Supplier.class), anyString()))
        .thenAnswer(
            invocation -> {
              Supplier<SeasonChampion> supplier = invocation.getArgument(0);
              return supplier.get();
            });
  }

  @Test
  void whenFetchingChampions_thenReturnChampionsForRange() {
    when(f1ApiClient.fetchChampionForSeason(2021)).thenReturn(champion2021);
    when(f1ApiClient.fetchChampionForSeason(2022)).thenReturn(champion2022);

    List<SeasonChampion> result = service.getSeasonChampions(new SeasonRangeRequest(2021, 2022));

    assertEquals(2, result.size());
    assertEquals("2021", result.get(0).getSeason());
    assertEquals("2022", result.get(1).getSeason());
    verify(f1ApiClient, times(2)).fetchChampionForSeason(anyInt());
  }

  @Test
  void whenApiCallFails_thenSkipFailedYear() {
    when(f1ApiClient.fetchChampionForSeason(2021)).thenReturn(champion2021);
    when(f1ApiClient.fetchChampionForSeason(2022)).thenThrow(new RuntimeException("API Error"));

    List<SeasonChampion> result = service.getSeasonChampions(new SeasonRangeRequest(2021, 2022));

    assertEquals(1, result.size());
    assertEquals("2021", result.get(0).getSeason());
    verify(f1ApiClient, times(2)).fetchChampionForSeason(anyInt());
  }

  @Test
  void whenSavingChampion_thenReturnAsIs() {
    SeasonChampion result = service.saveChampion(champion2021);

    assertEquals(champion2021, result);
    verifyNoInteractions(f1ApiClient);
  }
}
