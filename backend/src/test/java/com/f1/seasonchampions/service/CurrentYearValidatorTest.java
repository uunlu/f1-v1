package com.f1.seasonchampions.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Year;

import com.f1.seasonchampions.validation.CurrentYearValidator;
import org.junit.jupiter.api.Test;

class CurrentYearValidatorTest {

  private final CurrentYearValidator validator = new CurrentYearValidator();

  @Test
  void whenValueIsNull_thenIsValidReturnsTrue() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void whenValueIsCurrentYear_thenIsValidReturnsTrue() {
    int currentYear = Year.now().getValue();
    assertTrue(validator.isValid(currentYear, null));
  }

  @Test
  void whenValueIsPastYear_thenIsValidReturnsTrue() {
    int pastYear = Year.now().getValue() - 10;
    assertTrue(validator.isValid(pastYear, null));
  }

  @Test
  void whenValueIsFutureYear_thenIsValidReturnsFalse() {
    int futureYear = Year.now().getValue() + 1;
    assertFalse(validator.isValid(futureYear, null));
  }
}
