package com.f1.seasonchampions.model;

public class RaceWinner {
    private String season;
    private String round;
    private Driver driver;
    private Constructor constructor;
    private String time;

    public RaceWinner() {
        this.season = season;
        this.round = round;
        this.driver = driver;
        this.constructor = constructor;
        this.time = time;
    }

    public String getSeason() {
        return season;
    }

    public String getRound() {
        return round;
    }

    public Driver getDriver() {
        return driver;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public String getTime() {
        return time;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
