package com.f1.seasonchampions.controller;

import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.dto.RaceWinnerSeasonResponse;
import com.f1.seasonchampions.dto.SeasonChampionListItemImpl;
import com.f1.seasonchampions.service.query.racewinner.RaceWinnerQueryService;
import com.f1.seasonchampions.service.query.seasonchampion.SeasonChampionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/f1")
@Tag(
    name = "F1 Public API",
    description = "Public APIs for retrieving F1 race winners and season information")
@RequiredArgsConstructor
public class FormulaOneController {

  private final RaceWinnerQueryService raceWinnerQueryService;
  private final SeasonChampionQueryService seasonChampionQueryService;

  @GetMapping("/race-winners/{season}")
  @Operation(
      summary = "Get race winners for a specific season",
      description = "Retrieves all race winners from the database for the specified F1 season")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved race winners"),
        @ApiResponse(responseCode = "404", description = "No data found for the given season"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<RaceWinnerListItem>> getRaceWinnersBySeason(
      @Parameter(description = "Year of the F1 season (e.g., 2022)") @PathVariable
          final int season) {

    log.info("Received public request for race winners of season {}", season);
    final List<RaceWinnerListItem> winners = this.raceWinnerQueryService.getWinnersBySeason(season);
    log.info("Returning {} race winners for season {}", winners.size(), season);
    return ResponseEntity.ok(winners);
  }

  @GetMapping("/race-winners/{season}/metadata")
  @Operation(
      summary = "Get race winners with season metadata",
      description =
          """
        Retrieves race winners with additional season information including champion status and season conclusion status
        """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved race winners with metadata"),
        @ApiResponse(responseCode = "404", description = "No data found for the given season"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<RaceWinnerSeasonResponse> getRaceWinnersWithMetadata(
      @Parameter(description = "Year of the F1 season (e.g., 2022)") @PathVariable
          final int season) {

    log.info("Received public request for race winners with metadata for season {}", season);
    final RaceWinnerSeasonResponse response =
        this.raceWinnerQueryService.getWinnersWithSeasonMetadata(season);
    log.info(
        "Returning race winners with metadata for season {} - concluded: {}, hasChampion: {}",
        season,
        response.isSeasonConcluded(),
        response.isHasChampion());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/seasons")
  @Operation(
      summary = "Get all available F1 seasons",
      description = "Retrieves a list of all seasons for which data is available in the system")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved season list"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<SeasonChampionListItemImpl>> getAllSeasons() {
    log.info("Received public request for all available F1 seasons");
    final var seasons = this.seasonChampionQueryService.getAllSeasons();
    log.info("Returning {} seasons", seasons.size());
    return ResponseEntity.ok(seasons);
  }
}
