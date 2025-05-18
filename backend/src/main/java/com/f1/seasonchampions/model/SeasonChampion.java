package com.f1.seasonchampions.model;

public class SeasonChampion {
    private String season;
    private Driver driver;
    private Constructor constructor;

    public SeasonChampion() {}

    public SeasonChampion(String season, Driver driver, Constructor constructor) {
        this.season = season;
        this.driver = driver;
        this.constructor = constructor;
    }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public Constructor getConstructor() { return constructor; }
    public void setConstructor(Constructor constructor) { this.constructor = constructor; }
} 