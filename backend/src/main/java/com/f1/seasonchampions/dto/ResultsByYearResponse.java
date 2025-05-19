package com.f1.seasonchampions.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResultsByYearResponse {
    private MrData mrData;

    @Data
    public static class MrData {
        private RaceTable raceTable;
    }

    @Data
    public static class RaceTable {
        private String season;
        private List<Race> races;
    }

    @Data
    public static class Race {
        private String season;
        private String round;
        private String raceName;
        private Circuit circuit;
        private String date;
        private String time;
        private List<Result> results;
    }

    @Data
    public static class Circuit {
        private String circuitId;
        private String circuitName;
        private Location location;
    }

    @Data
    public static class Location {
        private String lat;
        private String longitude;
        private String locality;
        private String country;
    }

    @Data
    public static class Result {
        private int number;
        private int position;
        private String positionText;
        private int points;
        private Driver driver;
        private Constructor constructor;
        private int grid;
        private int laps;
        private String status;
        private Time time;
        private FastestLap fastestLap;
    }

    @Data
    public static class Driver {
        private String driverId;
        private String permanentNumber;
        private String code;
        private String givenName;
        private String familyName;
        private String dateOfBirth;
        private String nationality;
    }

    @Data
    public static class Constructor {
        private String constructorId;
        private String name;
        private String nationality;
    }

    @Data
    public static class Time {
        private String millis;
        private String time;
    }

    @Data
    public static class FastestLap {
        private int rank;
        private int lap;
        private Time time;
        private AverageSpeed averageSpeed;
    }

    @Data
    public static class AverageSpeed {
        private String units;
        private String speed;
    }
} 