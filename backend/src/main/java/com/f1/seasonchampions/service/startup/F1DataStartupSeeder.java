package com.f1.seasonchampions.service.startup;

import com.f1.seasonchampions.dto.RaceSyncResult;
import com.f1.seasonchampions.service.scheduler.F1DataSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Component responsible for seeding F1 data during application startup. Follows Single
 * Responsibility Principle by handling only startup data seeding.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F1DataStartupSeeder {

  private final F1DataSchedulerService schedulerService;
  private final F1DataSeedingStrategy seedingStrategy;

  /**
   * Seeds historical F1 race data when the application is ready. Only runs if seeding is needed
   * based on the configured strategy.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void seedHistoricalRaceData() {
    log.info("Application ready - checking if F1 data seeding is needed");

    if (!this.seedingStrategy.isSeedingNeeded()) {
      log.info("F1 data seeding not needed - database already contains race data");
      return;
    }

    log.info("Starting F1 historical data seeding (2005 to current year)");

    try {
      final RaceSyncResult seasonResult = this.schedulerService.syncSeasonChampions();
      if (seasonResult.success()) {
        log.info(
            "F1 data seeding completed successfully - {} season champions updated",
            seasonResult.updatedCount());
      } else {
        log.warn(
            "F1 data seeding season champions completed with issues: {}", seasonResult.message());
      }
    } catch (Exception e) {
      log.error("Failed to seed F1 season champions historical data during startup", e);
    }

    try {
      final RaceSyncResult result = this.schedulerService.syncAllHistoricalRaces();

      if (result.success()) {
        log.info(
            "F1 data seeding completed successfully - {} races updated", result.updatedCount());
      } else {
        log.warn("F1 data seeding completed with issues: {}", result.message());
      }
    } catch (Exception e) {
      log.error("Failed to seed F1 race winners historical data during startup", e);
    }
  }
}
