package com.f1.seasonchampions.model;

import com.f1.seasonchampions.validation.CurrentYearConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SeasonRangeRequest {

    @Min(value = 1950, message = "Start year must be 1950 or later")
    private int startYear;

    @CurrentYearConstraint
    private int endYear;

    public SeasonRangeRequest() {
        // Default constructor needed for deserialization
    }

    public SeasonRangeRequest(int startYear, int endYear) {
        this.startYear = startYear;
        this.endYear = endYear;
    }

    // Getters and setters
    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }
}