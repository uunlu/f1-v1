package com.f1.seasonchampions.controller;

import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.service.scheduler.F1DataSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/internal/")
@Tag(
    name = "F1 Admin API",
    description = "Admin/internal APIs for managing F1 data synchronization")
@RequiredArgsConstructor
public class AdminController {

  private final F1DataSchedulerService schedulerService;

  @PostMapping("/sync-latest-race-results")
  @Operation(
      summary = "Trigger synchronization of the latest F1 race results",
      description =
          "Manually triggers the background job to sync the latest F1 race winner data from the external API")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Sync process completed"),
        @ApiResponse(
            responseCode = "500",
            description = "Internal error while syncing race results")
      })
  public ResponseEntity<RaceSyncResult> triggerRaceResultSync() {
    log.info("Admin triggered manual sync for latest F1 race results");

    final RaceSyncResult result = this.schedulerService.syncLatestF1RaceResult();
    log.info("Sync completed with {} updates", result.updatedCount());

    if (result.success()) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
  }

  @PostMapping("/sync-season-champions")
  @Operation(
      summary = "Trigger synchronization of F1 season champions",
      description =
          "Manually triggers the background job to sync season champion data from the external API")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Sync process completed"),
        @ApiResponse(
            responseCode = "500",
            description = "Internal error while syncing season champions")
      })
  public ResponseEntity<RaceSyncResult> triggerSeasonChampionsSync() {
    log.info("Admin triggered manual sync for F1 season champions");

    final RaceSyncResult result = this.schedulerService.syncSeasonChampions();
    log.info("Season champions sync completed with {} updates", result.updatedCount());

    if (result.success()) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
  }
}
