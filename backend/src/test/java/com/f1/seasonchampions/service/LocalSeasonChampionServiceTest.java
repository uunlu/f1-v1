package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
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
class LocalSeasonChampionServiceTest {
  @Mock private SeasonChampionRepository seasonChampionRepository;

  @InjectMocks private LocalSeasonChampionService service;

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
  void whenGettingChampions_thenReturnFromRepository() {
    SeasonRangeRequest request = new SeasonRangeRequest(2021, 2022);
    List<SeasonChampion> expectedChampions = Arrays.asList(champion2021, champion2022);

    when(seasonChampionRepository.findBySeasonBetweenOrderBySeason("2021", "2022"))
        .thenReturn(expectedChampions);

    List<SeasonChampion> result = service.getSeasonChampions(request);

    assertEquals(expectedChampions, result);
    verify(seasonChampionRepository).findBySeasonBetweenOrderBySeason("2021", "2022");
  }

  @Test
  void whenSavingChampion_thenSaveToRepository() {
    when(seasonChampionRepository.save(champion2021)).thenReturn(champion2021);

    SeasonChampion result = service.saveChampion(champion2021);

    assertEquals(champion2021, result);
    verify(seasonChampionRepository).save(champion2021);
  }

  @Test
  void whenCheckingCompleteData_withAllYearsPresent_thenReturnTrue() {
    SeasonRangeRequest request = new SeasonRangeRequest(2021, 2022);
    List<SeasonChampion> champions = Arrays.asList(champion2021, champion2022);

    when(seasonChampionRepository.findBySeasonBetweenOrderBySeason("2021", "2022"))
        .thenReturn(champions);

    boolean result = service.hasCompleteDataForRange(request);

    assertTrue(result);
  }

  @Test
  void whenCheckingCompleteData_withMissingYears_thenReturnFalse() {
    SeasonRangeRequest request = new SeasonRangeRequest(2021, 2022);
    List<SeasonChampion> champions = Collections.singletonList(champion2021);

    when(seasonChampionRepository.findBySeasonBetweenOrderBySeason("2021", "2022"))
        .thenReturn(champions);

    boolean result = service.hasCompleteDataForRange(request);

    assertFalse(result);
  }
}
