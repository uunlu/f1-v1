package com.f1.seasonchampions.service.racewinner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
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
class LocalRaceWinnerSeedServiceTest {

  @Mock private RaceWinnerRepository raceWinnerRepository;
  @Mock private DriverRepository driverRepository;
  @Mock private ConstructorRepository constructorRepository;

  @InjectMocks private LocalRaceWinnerSeedService service;

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
  void whenGettingRaceWinners_thenReturnFromRepository() {
    List<RaceWinner> expectedWinners = Arrays.asList(raceWinner2023);
    when(raceWinnerRepository.findBySeasonAndOptionalRound("2023", null))
        .thenReturn(expectedWinners);

    List<RaceWinner> result = service.getRaceWinners(2023);

    assertEquals(expectedWinners, result);
    verify(raceWinnerRepository).findBySeasonAndOptionalRound("2023", null);
  }

  @Test
  void whenSavingNewRaceWinner_withNewDriverAndConstructor_thenSaveAll() {
    when(raceWinnerRepository.findBySeasonAndOptionalRound("2023", "1"))
        .thenReturn(Collections.emptyList());
    when(driverRepository.findById("max_verstappen")).thenReturn(Optional.empty());
    when(constructorRepository.findByConstructorId("red_bull")).thenReturn(Optional.empty());
    when(driverRepository.save(driver)).thenReturn(driver);
    when(constructorRepository.save(constructor)).thenReturn(constructor);
    when(raceWinnerRepository.save(raceWinner2023)).thenReturn(raceWinner2023);

    RaceWinner result = service.saveRaceWinner(raceWinner2023);

    assertEquals(raceWinner2023, result);
    verify(driverRepository).save(driver);
    verify(constructorRepository).save(constructor);
    verify(raceWinnerRepository).save(raceWinner2023);
  }

  @Test
  void whenSavingRaceWinner_withExistingDriverAndConstructor_thenReuseEntities() {
    when(raceWinnerRepository.findBySeasonAndOptionalRound("2023", "1"))
        .thenReturn(Collections.emptyList());
    when(driverRepository.findById("max_verstappen")).thenReturn(Optional.of(driver));
    when(constructorRepository.findByConstructorId("red_bull"))
        .thenReturn(Optional.of(constructor));
    when(raceWinnerRepository.save(raceWinner2023)).thenReturn(raceWinner2023);

    RaceWinner result = service.saveRaceWinner(raceWinner2023);

    assertEquals(raceWinner2023, result);
    verify(driverRepository, never()).save(any());
    verify(constructorRepository, never()).save(any());
    verify(raceWinnerRepository).save(raceWinner2023);
  }

  @Test
  void whenSavingDuplicateRaceWinner_thenReturnExisting() {
    when(raceWinnerRepository.findBySeasonAndOptionalRound("2023", "1"))
        .thenReturn(Collections.singletonList(raceWinner2023));

    RaceWinner result = service.saveRaceWinner(raceWinner2023);

    assertEquals(raceWinner2023, result);
    verify(driverRepository, never()).save(any());
    verify(constructorRepository, never()).save(any());
    verify(raceWinnerRepository, never()).save(any());
  }

  @Test
  void whenCheckingCompleteData_withExistingData_thenReturnTrue() {
    when(raceWinnerRepository.findBySeasonAndOptionalRound("2023", null))
        .thenReturn(Collections.singletonList(raceWinner2023));

    boolean result = service.hasCompleteDataForYear(2023);

    assertTrue(result);
  }

  @Test
  void whenCheckingCompleteData_withNoData_thenReturnFalse() {
    when(raceWinnerRepository.findBySeasonAndOptionalRound("2023", null))
        .thenReturn(Collections.emptyList());

    boolean result = service.hasCompleteDataForYear(2023);

    assertFalse(result);
  }
}
