package com.f1.seasonchampions.model;

public class SeasonChampion {
    private int season;
    private Driver driver;
    private Constructor constructor;

    public SeasonChampion() {}

    public SeasonChampion(int season, Driver driver, Constructor constructor) {
        this.season = season;
        this.driver = driver;
        this.constructor = constructor;
    }

    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public Constructor getConstructor() { return constructor; }
    public void setConstructor(Constructor constructor) { this.constructor = constructor; }
} 