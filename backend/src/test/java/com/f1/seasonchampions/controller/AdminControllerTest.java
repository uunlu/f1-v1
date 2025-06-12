package com.f1.seasonchampions.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.service.scheduler.F1DataSchedulerService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

  @Mock private F1DataSchedulerService schedulerService;

  private AdminController adminController;

  @BeforeEach
  void setUp() {
    adminController = new AdminController(schedulerService);
  }

  @Test
  void triggerRaceResultSync_whenSuccessfulWithUpdates_returnsCreatedStatus() {
    // Arrange
    var expectedResult =
        new RaceSyncResult(2, List.of("Race 1", "Race 2"), true, "Successfully synced 2 races");
    when(schedulerService.syncLatestF1RaceResult()).thenReturn(expectedResult);

    // Act
    ResponseEntity<RaceSyncResult> response = adminController.triggerRaceResultSync();

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().updatedCount());
    assertEquals(List.of("Race 1", "Race 2"), response.getBody().updatedRaces());
    assertTrue(response.getBody().success());
    assertEquals("Successfully synced 2 races", response.getBody().message());

    verify(schedulerService).syncLatestF1RaceResult();
  }

  @Test
  void triggerRaceResultSync_whenSuccessfulWithNoUpdates_returnsOkStatus() {
    // Arrange
    var expectedResult = new RaceSyncResult(0, List.of(), true, "No new races to sync");
    when(schedulerService.syncLatestF1RaceResult()).thenReturn(expectedResult);

    // Act
    ResponseEntity<RaceSyncResult> response = adminController.triggerRaceResultSync();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().updatedCount());
    assertEquals(List.of(), response.getBody().updatedRaces());
    assertTrue(response.getBody().success());
    assertEquals("No new races to sync", response.getBody().message());

    verify(schedulerService).syncLatestF1RaceResult();
  }

  @Test
  void triggerRaceResultSync_whenSyncFails_returnsInternalServerErrorStatus() {
    // Arrange
    var expectedResult = new RaceSyncResult(0, List.of(), false, "API connection failed");
    when(schedulerService.syncLatestF1RaceResult()).thenReturn(expectedResult);

    // Act
    ResponseEntity<RaceSyncResult> response = adminController.triggerRaceResultSync();

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().updatedCount());
    assertFalse(response.getBody().success());
    assertEquals("API connection failed", response.getBody().message());

    verify(schedulerService).syncLatestF1RaceResult();
  }

  @Test
  void triggerSeasonChampionsSync_whenSuccessfulWithUpdates_returnsCreatedStatus() {
    // Arrange
    var expectedResult =
        new RaceSyncResult(
            3, List.of("2022", "2023", "2024"), true, "Successfully synced 3 champions");
    when(schedulerService.syncSeasonChampions()).thenReturn(expectedResult);

    // Act
    ResponseEntity<RaceSyncResult> response = adminController.triggerSeasonChampionsSync();

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(3, response.getBody().updatedCount());
    assertEquals(List.of("2022", "2023", "2024"), response.getBody().updatedRaces());
    assertTrue(response.getBody().success());
    assertEquals("Successfully synced 3 champions", response.getBody().message());

    verify(schedulerService).syncSeasonChampions();
  }

  @Test
  void triggerSeasonChampionsSync_whenSuccessfulWithNoUpdates_returnsOkStatus() {
    // Arrange
    var expectedResult = new RaceSyncResult(0, List.of(), true, "No new champions to sync");
    when(schedulerService.syncSeasonChampions()).thenReturn(expectedResult);

    // Act
    ResponseEntity<RaceSyncResult> response = adminController.triggerSeasonChampionsSync();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().updatedCount());
    assertEquals(List.of(), response.getBody().updatedRaces());
    assertTrue(response.getBody().success());
    assertEquals("No new champions to sync", response.getBody().message());

    verify(schedulerService).syncSeasonChampions();
  }

  @Test
  void triggerSeasonChampionsSync_whenSyncFails_returnsInternalServerErrorStatus() {
    // Arrange
    var expectedResult = new RaceSyncResult(0, List.of(), false, "External API timeout");
    when(schedulerService.syncSeasonChampions()).thenReturn(expectedResult);

    // Act
    ResponseEntity<RaceSyncResult> response = adminController.triggerSeasonChampionsSync();

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().updatedCount());
    assertFalse(response.getBody().success());
    assertEquals("External API timeout", response.getBody().message());

    verify(schedulerService).syncSeasonChampions();
  }

  @Test
  void triggerRaceResultSync_verifyServiceInteraction() {
    // Arrange
    var expectedResult = new RaceSyncResult(1, List.of("Race 1"), true, "Success");
    when(schedulerService.syncLatestF1RaceResult()).thenReturn(expectedResult);

    // Act
    adminController.triggerRaceResultSync();

    // Assert
    verify(schedulerService, times(1)).syncLatestF1RaceResult();
    verifyNoMoreInteractions(schedulerService);
  }

  @Test
  void triggerSeasonChampionsSync_verifyServiceInteraction() {
    // Arrange
    var expectedResult = new RaceSyncResult(1, List.of("2024"), true, "Success");
    when(schedulerService.syncSeasonChampions()).thenReturn(expectedResult);

    // Act
    adminController.triggerSeasonChampionsSync();

    // Assert
    verify(schedulerService, times(1)).syncSeasonChampions();
    verifyNoMoreInteractions(schedulerService);
  }
}
