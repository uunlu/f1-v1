package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import com.f1.seasonchampions.service.seed.seasonchampion.LocalSeasonChampionSeedService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalSeasonChampionSeedServiceTest {
  @Mock private SeasonChampionRepository seasonChampionRepository;
  @Mock private ConstructorRepository constructorRepository;
  @Mock private DriverRepository driverRepository;

  @InjectMocks private LocalSeasonChampionSeedService service;

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
    when(constructorRepository.findByConstructorId("red_bull")).thenReturn(Optional.empty());
    when(driverRepository.findById("max_verstappen")).thenReturn(Optional.empty());
    when(constructorRepository.save(any(Constructor.class)))
        .thenReturn(champion2021.getConstructor());
    when(driverRepository.save(any(Driver.class))).thenReturn(champion2021.getDriver());
    when(seasonChampionRepository.save(champion2021)).thenReturn(champion2021);

    SeasonChampion result = service.saveChampion(champion2021);

    assertEquals(champion2021, result);
    verify(seasonChampionRepository).save(champion2021);
    verify(constructorRepository).save(any(Constructor.class));
    verify(driverRepository).save(any(Driver.class));
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
