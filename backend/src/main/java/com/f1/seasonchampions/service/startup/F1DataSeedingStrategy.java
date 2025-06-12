package com.f1.seasonchampions.service.startup;

/**
 * Strategy interface for determining when F1 data seeding is needed. Follows Interface Segregation
 * Principle by providing a focused contract.
 */
public interface F1DataSeedingStrategy {

  /**
   * Determines if F1 data seeding is needed based on current database state.
   *
   * @return true if seeding is needed, false otherwise
   */
  boolean isSeedingNeeded();
}
