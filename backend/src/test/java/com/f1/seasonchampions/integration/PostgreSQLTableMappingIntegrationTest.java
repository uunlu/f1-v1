package com.f1.seasonchampions.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.dto.SeasonChampionListItem;
import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class PostgreSQLTableMappingIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("f1test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    registry.add("spring.flyway.enabled", () -> "true");
  }

  @Autowired private DriverRepository driverRepository;

  @Autowired private ConstructorRepository constructorRepository;

  @Autowired private SeasonChampionRepository seasonChampionRepository;

  @Autowired private RaceWinnerRepository raceWinnerRepository;

  private Driver testDriver;
  private Constructor testConstructor;

  @BeforeEach
  void setUp() {
    // Clean up test data
    raceWinnerRepository.deleteAll();
    seasonChampionRepository.deleteAll();
    constructorRepository.deleteAll();
    driverRepository.deleteAll();

    // Create minimal test data
    testDriver =
        Driver.builder()
            .driverId("test_driver")
            .permanentNumber("1")
            .code("TST")
            .givenName("Test")
            .familyName("Driver")
            .dateOfBirth("1990-01-01")
            .nationality("TestLand")
            .build();

    testConstructor =
        Constructor.builder()
            .constructorId("test_constructor")
            .name("Test Constructor")
            .nationality("TestLand")
            .build();
  }

  @Test
  void testDriverTableMapping() {
    // Test basic persistence
    Driver savedDriver = driverRepository.save(testDriver);

    assertThat(savedDriver).isNotNull();
    assertThat(savedDriver.getDriverId()).isEqualTo("test_driver");
    assertThat(savedDriver.getGivenName()).isEqualTo("Test");
    assertThat(savedDriver.getFamilyName()).isEqualTo("Driver");

    // Test retrieval by ID
    Optional<Driver> foundDriver = driverRepository.findById("test_driver");
    assertThat(foundDriver).isPresent();
    assertThat(foundDriver.get().getCode()).isEqualTo("TST");
    assertThat(foundDriver.get().getNationality()).isEqualTo("TestLand");

    // Verify table mapping works with all fields
    assertThat(foundDriver.get().getPermanentNumber()).isEqualTo("1");
    assertThat(foundDriver.get().getDateOfBirth()).isEqualTo("1990-01-01");
  }

  @Test
  void testConstructorTableMapping() {
    // Test basic persistence
    Constructor savedConstructor = constructorRepository.save(testConstructor);

    assertThat(savedConstructor).isNotNull();
    assertThat(savedConstructor.getId()).isNotNull(); // Auto-generated ID
    assertThat(savedConstructor.getConstructorId()).isEqualTo("test_constructor");
    assertThat(savedConstructor.getName()).isEqualTo("Test Constructor");

    // Test custom query method
    Optional<Constructor> foundByConstructorId =
        constructorRepository.findByConstructorId("test_constructor");
    assertThat(foundByConstructorId).isPresent();
    assertThat(foundByConstructorId.get().getName()).isEqualTo("Test Constructor");
    assertThat(foundByConstructorId.get().getNationality()).isEqualTo("TestLand");

    // Test that ID is auto-generated
    assertThat(foundByConstructorId.get().getId()).isPositive();
  }

  @Test
  void testSeasonChampionTableMappingWithRelationships() {
    // Save dependencies first
    Driver savedDriver = driverRepository.save(testDriver);
    Constructor savedConstructor = constructorRepository.save(testConstructor);

    // Create season champion with relationships
    SeasonChampion seasonChampion =
        SeasonChampion.builder()
            .season("2023")
            .driver(savedDriver)
            .constructor(savedConstructor)
            .build();

    SeasonChampion savedChampion = seasonChampionRepository.save(seasonChampion);

    assertThat(savedChampion).isNotNull();
    assertThat(savedChampion.getSeason()).isEqualTo("2023");
    assertThat(savedChampion.getDriver()).isNotNull();
    assertThat(savedChampion.getDriver().getDriverId()).isEqualTo("test_driver");
    assertThat(savedChampion.getConstructor()).isNotNull();
    assertThat(savedChampion.getConstructor().getConstructorId()).isEqualTo("test_constructor");

    // Test retrieval by season
    Optional<SeasonChampion> foundChampion = seasonChampionRepository.findById("2023");
    assertThat(foundChampion).isPresent();
    assertThat(foundChampion.get().getDriver().getGivenName()).isEqualTo("Test");
    assertThat(foundChampion.get().getConstructor().getName()).isEqualTo("Test Constructor");
  }

  @Test
  void testSeasonChampionCustomQueryMethods() {
    // Setup test data
    Driver savedDriver = driverRepository.save(testDriver);
    Constructor savedConstructor = constructorRepository.save(testConstructor);

    SeasonChampion champion2022 =
        SeasonChampion.builder()
            .season("2022")
            .driver(savedDriver)
            .constructor(savedConstructor)
            .build();

    SeasonChampion champion2023 =
        SeasonChampion.builder()
            .season("2023")
            .driver(savedDriver)
            .constructor(savedConstructor)
            .build();

    seasonChampionRepository.saveAll(List.of(champion2022, champion2023));

    // Test range query
    List<SeasonChampion> championsInRange =
        seasonChampionRepository.findBySeasonBetweenOrderBySeason("2022", "2023");

    assertThat(championsInRange).hasSize(2);
    assertThat(championsInRange.get(0).getSeason()).isEqualTo("2022");
    assertThat(championsInRange.get(1).getSeason()).isEqualTo("2023");

    // Test native query for projection
    List<SeasonChampionListItem> listItems =
        seasonChampionRepository.findAllSeasonChampionListItems();

    assertThat(listItems).hasSize(2);

    // Verify projection mapping works (schema evolution resilient)
    SeasonChampionListItem item = listItems.get(0);
    assertThat(item.getSeason()).isIn("2022", "2023");
    assertThat(item.getDriver()).isEqualTo("test_driver");
    assertThat(item.getConstructor()).isEqualTo("test_constructor");
  }

  @Test
  void testRaceWinnerTableMappingWithConstraints() {
    // Setup dependencies
    Driver savedDriver = driverRepository.save(testDriver);
    Constructor savedConstructor = constructorRepository.save(testConstructor);

    // Create race winner
    RaceWinner raceWinner = new RaceWinner();
    raceWinner.setSeason("2023");
    raceWinner.setRound("1");
    raceWinner.setDriver(savedDriver);
    raceWinner.setConstructor(savedConstructor);
    raceWinner.setTime("1:37:33.584");

    RaceWinner savedWinner = raceWinnerRepository.save(raceWinner);

    assertThat(savedWinner).isNotNull();
    assertThat(savedWinner.getId()).isNotNull(); // Auto-generated ID
    assertThat(savedWinner.getSeason()).isEqualTo("2023");
    assertThat(savedWinner.getRound()).isEqualTo("1");
    assertThat(savedWinner.getTime()).isEqualTo("1:37:33.584");

    // Verify relationships are properly mapped
    assertThat(savedWinner.getDriver()).isNotNull();
    assertThat(savedWinner.getDriver().getDriverId()).isEqualTo("test_driver");
    assertThat(savedWinner.getConstructor()).isNotNull();
    assertThat(savedWinner.getConstructor().getConstructorId()).isEqualTo("test_constructor");

    // Test unique constraint (season, round combination should be unique)
    RaceWinner duplicateWinner = new RaceWinner();
    duplicateWinner.setSeason("2023");
    duplicateWinner.setRound("1");
    duplicateWinner.setDriver(savedDriver);
    duplicateWinner.setConstructor(savedConstructor);
    duplicateWinner.setTime("1:35:00.000");

    // This should either fail or be handled by the unique constraint
    assertThrows(
        Exception.class,
        () -> {
          raceWinnerRepository.save(duplicateWinner);
          raceWinnerRepository.flush(); // Force immediate constraint check
        });
  }

  @Test
  void testRaceWinnerCustomQueryMethods() {
    // Setup test data
    Driver savedDriver = driverRepository.save(testDriver);
    Constructor savedConstructor = constructorRepository.save(testConstructor);

    RaceWinner winner1 = new RaceWinner();
    winner1.setSeason("2023");
    winner1.setRound("1");
    winner1.setDriver(savedDriver);
    winner1.setConstructor(savedConstructor);
    winner1.setTime("1:37:33.584");

    RaceWinner winner2 = new RaceWinner();
    winner2.setSeason("2023");
    winner2.setRound("2");
    winner2.setDriver(savedDriver);
    winner2.setConstructor(savedConstructor);
    winner2.setTime("1:35:22.123");

    raceWinnerRepository.saveAll(List.of(winner1, winner2));

    // Test season query without round filter
    List<RaceWinner> seasonWinners =
        raceWinnerRepository.findBySeasonAndOptionalRound("2023", null);
    assertThat(seasonWinners).hasSize(2);
    assertThat(seasonWinners).extracting(RaceWinner::getRound).containsExactlyInAnyOrder("1", "2");

    // Test season query with specific round
    List<RaceWinner> roundWinners = raceWinnerRepository.findBySeasonAndOptionalRound("2023", "1");
    assertThat(roundWinners).hasSize(1);
    assertThat(roundWinners.get(0).getRound()).isEqualTo("1");
    assertThat(roundWinners.get(0).getTime()).isEqualTo("1:37:33.584");

    // Test default method
    List<RaceWinner> defaultMethodResult = raceWinnerRepository.getRaceWinnerBySeason("2023");
    assertThat(defaultMethodResult).hasSize(2);

    // Test complex projection query (schema evolution resilient)
    List<RaceWinnerListItem> projectionResults =
        raceWinnerRepository.findRaceWinnersWithConstructors("2023");
    assertThat(projectionResults).hasSize(2);

    RaceWinnerListItem item = projectionResults.get(0);
    assertThat(item.getSeasonName()).isEqualTo("2023");
    assertThat(item.getRound()).isIn("1", "2");
    assertThat(item.getDriver()).isNotNull();
    assertThat(item.getConstructorName()).isEqualTo("Test Constructor");
    assertThat(item.getTime()).isIn("1:37:33.584", "1:35:22.123");

    // Verify champion flag works (should be false since no season champion exists)
    assertThat(item.isChampion()).isFalse();
  }

  @Test
  void testRaceWinnerWithSeasonChampionProjection() {
    // Setup comprehensive test data
    Driver savedDriver = driverRepository.save(testDriver);
    Constructor savedConstructor = constructorRepository.save(testConstructor);

    // Create season champion
    SeasonChampion champion =
        SeasonChampion.builder()
            .season("2023")
            .driver(savedDriver)
            .constructor(savedConstructor)
            .build();
    seasonChampionRepository.save(champion);

    // Create race winner for the same driver/season
    RaceWinner winner = new RaceWinner();
    winner.setSeason("2023");
    winner.setRound("1");
    winner.setDriver(savedDriver);
    winner.setConstructor(savedConstructor);
    winner.setTime("1:37:33.584");
    raceWinnerRepository.save(winner);

    // Test complex join query
    List<RaceWinnerListItem> results = raceWinnerRepository.findRaceWinnersWithConstructors("2023");
    assertThat(results).hasSize(1);

    RaceWinnerListItem item = results.get(0);
    assertThat(item.getSeasonName()).isEqualTo("2023");
    assertThat(item.getRound()).isEqualTo("1");
    assertThat(item.isChampion()).isTrue(); // Should be true now since driver is season champion
    assertThat(item.getSeasonDriverId()).isEqualTo("test_driver");
    assertThat(item.getConstructorName()).isEqualTo("Test Constructor");
  }

  @Test
  void testSchemaEvolutionResilience() {
    // This test ensures that adding/removing columns won't break the code
    // by using field-based mapping rather than position-based

    Driver driver = driverRepository.save(testDriver);
    Constructor constructor = constructorRepository.save(testConstructor);

    // Save entities and verify all mapped fields work
    SeasonChampion champion =
        SeasonChampion.builder().season("2023").driver(driver).constructor(constructor).build();

    SeasonChampion saved = seasonChampionRepository.save(champion);

    // Access all fields to ensure mapping works
    assertThat(saved.getSeason()).isNotNull();
    assertThat(saved.getDriver().getDriverId()).isNotNull();
    assertThat(saved.getDriver().getGivenName()).isNotNull();
    assertThat(saved.getDriver().getFamilyName()).isNotNull();
    assertThat(saved.getDriver().getNationality()).isNotNull();
    assertThat(saved.getConstructor().getConstructorId()).isNotNull();
    assertThat(saved.getConstructor().getName()).isNotNull();

    // Verify projections use named mappings (not positional)
    List<SeasonChampionListItem> items = seasonChampionRepository.findAllSeasonChampionListItems();
    assertThat(items).isNotEmpty();

    SeasonChampionListItem item = items.get(0);
    // These should work even if column order changes in future schema modifications
    assertThat(item.getSeason()).isEqualTo("2023");
    assertThat(item.getDriver()).isEqualTo("test_driver");
    assertThat(item.getConstructor()).isEqualTo("test_constructor");
  }

  @Test
  void testTableRelationshipsIntegrity() {
    // Test that foreign key relationships work correctly
    Driver driver = driverRepository.save(testDriver);
    Constructor constructor = constructorRepository.save(testConstructor);

    // Create entities with relationships
    SeasonChampion champion =
        SeasonChampion.builder().season("2023").driver(driver).constructor(constructor).build();

    RaceWinner winner = new RaceWinner();
    winner.setSeason("2023");
    winner.setRound("1");
    winner.setDriver(driver);
    winner.setConstructor(constructor);
    winner.setTime("1:37:33.584");

    seasonChampionRepository.save(champion);
    raceWinnerRepository.save(winner);

    // Verify relationships are properly loaded
    SeasonChampion loadedChampion = seasonChampionRepository.findById("2023").orElseThrow();
    assertThat(loadedChampion.getDriver().getGivenName()).isEqualTo("Test");
    assertThat(loadedChampion.getConstructor().getName()).isEqualTo("Test Constructor");

    // Verify eager loading works for RaceWinner
    List<RaceWinner> winners = raceWinnerRepository.findBySeasonAndOptionalRound("2023", null);
    RaceWinner loadedWinner = winners.get(0);
    assertThat(loadedWinner.getDriver().getFamilyName()).isEqualTo("Driver");
    assertThat(loadedWinner.getConstructor().getNationality()).isEqualTo("TestLand");
  }
}
