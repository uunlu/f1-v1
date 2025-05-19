package com.f1.seasonchampions.dto;

import lombok.Data;
import java.util.List;

@Data
public class DriverStandingsByYearResponse {
    private MrData mrData;

    @Data
    public static class MrData {
        private StandingsTable standingsTable;
    }

    @Data
    public static class StandingsTable {
        private String season;
        private List<StandingsList> standingsLists;
    }

    @Data
    public static class StandingsList {
        private List<DriverStanding> driverStandings;
    }

    @Data
    public static class DriverStanding {
        private Driver driver;
        private List<Constructor> constructors;
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
} 