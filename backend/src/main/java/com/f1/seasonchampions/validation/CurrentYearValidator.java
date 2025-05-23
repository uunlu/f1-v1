package com.f1.seasonchampions.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

public class CurrentYearValidator implements ConstraintValidator<CurrentYearConstraint, Integer> {

  @Override
  public boolean isValid(final Integer value, final ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return value <= Year.now().getValue();
  }
}
