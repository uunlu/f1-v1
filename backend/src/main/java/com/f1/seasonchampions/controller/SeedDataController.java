package com.f1.seasonchampions.controller;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.service.seed.racewinner.RaceWinnerSeedService;
import com.f1.seasonchampions.service.seed.seasonchampion.SeasonChampionSeedService;
import com.f1.seasonchampions.validation.CurrentYearConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/seed-data")
@Tag(
    name = "F1 Season Champions",
    description = "APIs for retrieving F1 season champions and race winners")
@RequiredArgsConstructor
public class SeedDataController {

  private final SeasonChampionSeedService seasonChampionSeedService;
  private final RaceWinnerSeedService raceWinnerSeedService;

  @GetMapping(value = "/seasons", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get all F1 seasons",
      description = "Retrieves all available F1 seasons with basic information")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all seasons"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<List<SeasonChampion>> getAllSeasons() {
    log.info("Received request for all F1 seasons");
    final List<SeasonChampion> seasons = this.seasonChampionSeedService.getAllSeasonChampions();
    log.info("Returning {} seasons", seasons.size());
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(seasons);
  }

  @GetMapping(value = "/season-champions", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get season champions",
      description = "Retrieves F1 season champions for a given year range")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved season champions"),
        @ApiResponse(responseCode = "400", description = "Invalid year range provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<List<SeasonChampion>> getSeasonChampions(
      @Parameter(description = "Start year (default: 2005, min: 1950)")
          @RequestParam(value = "start_year", defaultValue = "2005")
          @Min(value = 1950, message = "Start year must be 1950 or later")
          final int startYear,
      @Parameter(description = "End year (default: 2024, max: current year)")
          @RequestParam(value = "end_year", defaultValue = "2024")
          @CurrentYearConstraint
          final int endYear) {

    log.info("Received request for season champions from {} to {}", startYear, endYear);
    final SeasonRangeRequest request = new SeasonRangeRequest(startYear, endYear);
    final List<SeasonChampion> champions =
        this.seasonChampionSeedService.getSeasonChampions(request);
    log.info("Returning {} season champions", champions.size());
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(champions);
  }

  @GetMapping(value = "/race-results", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get race winners",
      description = "Retrieves F1 race winners for a given year")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved race winners"),
        @ApiResponse(responseCode = "400", description = "Invalid year provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<List<RaceWinner>> getRaceResults(
      @Parameter(description = "Year to fetch race results for (default: 2005)")
          @RequestParam(defaultValue = "2005")
          final int year) {

    log.info("Received request for race winners for year {}", year);
    final List<RaceWinner> winners = this.raceWinnerSeedService.getRaceWinners(year);
    log.info("Returning {} race winners", winners.size());
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(winners);
  }
}
