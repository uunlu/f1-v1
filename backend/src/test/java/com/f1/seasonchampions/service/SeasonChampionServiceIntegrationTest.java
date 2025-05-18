package com.f1.seasonchampions.service;

import com.f1.seasonchampions.model.SeasonChampion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SeasonChampionServiceIntegrationTest {

    @Autowired
    private SeasonChampionService seasonChampionService;

    @Test
    public void testGetSeasonChampions() {
        List<SeasonChampion> champions = seasonChampionService.getSeasonChampions(2005, 2024);

        assertNotNull(champions);
        assertEquals(20, champions.size()); // 2005 to 2024

        SeasonChampion firstChampion = champions.get(0);
        assertEquals(2005, firstChampion.getSeason());
        assertNotNull(firstChampion.getDriver());
        assertNotNull(firstChampion.getConstructor());
    }

    @Test
    public void testApiErrorHandling() {
        // Simulate API error and validate proper handling
        // This can be implemented with MockRestServiceServer or similar in a more advanced test
    }
} 