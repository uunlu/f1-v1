package com.f1.seasonchampions.service.scheduler;

import com.f1.seasonchampions.model.RaceWinner;
import com.f1.seasonchampions.repository.RaceWinnerRepository;
import com.f1.seasonchampions.service.racewinner.RaceWinnerService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.Optional;

@Service
public class F1DataSchedulerService {
  private static final Logger logger = LoggerFactory.getLogger(F1DataSchedulerService.class);

  private final RaceWinnerService raceWinnerService;
  private final RaceWinnerRepository raceResultRepository;

  public F1DataSchedulerService(RaceWinnerService raceWinnerService, RaceWinnerRepository raceResultRepository) {
    this.raceWinnerService = raceWinnerService;
    this.raceResultRepository = raceResultRepository;
  }

  @Scheduled(cron = "${f1.data.sync.cron:0 0 12 * * ?}")
  @Transactional
  public void syncLatestF1RaceResult() {
    logger.info("Starting F1DataSchedulerService syncLatestF1RaceResult");

    try {
      var currentYear = Year.now().getValue();

      Optional<Integer> lastProcessedRound = getLastProcessedRound(currentYear);

      logger.info("Last processed round for year {}: {}", currentYear,
        lastProcessedRound.isPresent() ? lastProcessedRound.get() : "none");


      logger.info("Last processed race round: {}", lastProcessedRound);

      var currentYearWinners = raceWinnerService.getRaceWinners(currentYear);
      if (currentYearWinners.isEmpty()) {
        logger.info("No race winners found");
        return;
      }

      int newRacesProcessed = 0;

      for (RaceWinner raceWinner : currentYearWinners) {
        if (!lastProcessedRound.isPresent() || Integer.parseInt(raceWinner.getRound()) > lastProcessedRound.get()) {
          raceResultRepository.save(raceWinner);
          newRacesProcessed++;
          logger.info("Saved new race winner result: Season {} Round {} ", raceWinner.getSeason(), raceWinner.getRound());
        }
      }

      if (newRacesProcessed > 0) {
        logger.info("Successfully sync {} latest F1 race results", newRacesProcessed);
      } else {
        logger.info("No race winners found");
      }
    } catch (Exception e) {
      logger.error("F1DataSchedulerService error sync race data: {}", e.getMessage(), e);
    }
  }

  private Optional<Integer> getLastProcessedRound(final int year) {
    return raceResultRepository.findBySeason(String.valueOf(year))
      .stream()
      .map(raceWinner -> Integer.parseInt(raceWinner.getRound()))
      .max(Comparator.naturalOrder());
  }
}

