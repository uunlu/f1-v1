// package com.f1.seasonchampions.service;

// import static org.junit.jupiter.api.Assertions.*;

// import com.f1.seasonchampions.model.SeasonChampion;
// import com.f1.seasonchampions.model.SeasonRangeRequest;
// import java.util.List;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest
// public class SeasonSeasonChampionServiceIntegrationTest {

//   @Autowired private SeasonChampionService seasonChampionService;

//   @Test
//   public void testGetSeasonChampions() {
//     List<SeasonChampion> champions =
//         seasonChampionService.getSeasonChampions(new SeasonRangeRequest(2005, 2024));

//     assertNotNull(champions);
//     assertEquals(20, champions.size());

//     SeasonChampion firstChampion = champions.get(0);
//     assertEquals(2005, Integer.parseInt(firstChampion.getSeason()));

//     assertNotNull(firstChampion.getDriver());
//     assertNotNull(firstChampion.getConstructor());
//   }

//   @Test
//   public void testApiErrorHandling() {
//     // Simulate API error and validate proper handling
//     // This can be implemented with MockRestServiceServer or similar in a more advanced test
//   }
// }
