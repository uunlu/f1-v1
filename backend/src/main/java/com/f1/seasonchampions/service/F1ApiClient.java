package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.SeasonChampion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class F1ApiClient {

    private final RestTemplate restTemplate;

    @Value("https://api.jolpi.ca/ergast/")
    private String baseUrl;

    /**
     * Fetches the Formula 1 champion for a specific season
     *
     * @param year The year/season to fetch the champion for
     * @return SeasonChampion object with the champion's details
     */
    public SeasonChampion fetchChampionForSeason(int year) {
        log.info("Fetching F1 champion data for year: {}", year);

        try {
            String url = String.format("%s/f1/%d/driverstandings/", baseUrl, year);

            // This assumes the API returns a response that can be directly mapped to SeasonChampion
            // In practice, you might need to map from a different DTO structure
            F1ApiResponse response = restTemplate.getForObject(url, F1ApiResponse.class);

            if (response != null && response.getDriver() != null) {
                return mapToSeasonChampion(response, String.valueOf(year));
            } else {
                log.warn("No champion data found for year: {}", year);
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching F1 champion for year {}: {}", year, e.getMessage());
            throw new F1ApiException("Failed to fetch champion data for year: " + year, e);
        }
    }

    private SeasonChampion mapToSeasonChampion(F1ApiResponse response, String year) {
        DriverData driverData = response.getDriver();
        Driver driver = new Driver();
        driver.setDriverId(driverData.getDriverId());  // assuming this exists in DriverData
        driver.setCode(driverData.getCode());          // assuming this exists in DriverData
        driver.setGivenName(driverData.getFirstName());
        driver.setFamilyName(driverData.getLastName());
        driver.setNationality(driverData.getNationality());

        return SeasonChampion.builder()
                .season(String.valueOf(year))
                .driver(driver)
                .build();
    }

    // Inner classes for API response structure
    @lombok.Data
    private static class F1ApiResponse {
        private DriverData driver;
        private TeamData team;
        private int points;
        private int wins;
    }

    @lombok.Data
    private static class DriverData {
        private String driverId;
        private String firstName;
        private String lastName;
        private String nationality;
        private String code;
    }

    @lombok.Data
    private static class TeamData {
        private String teamId;
        private String name;
    }

    // Custom exception class for API errors
    public static class F1ApiException extends RuntimeException {
        public F1ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
