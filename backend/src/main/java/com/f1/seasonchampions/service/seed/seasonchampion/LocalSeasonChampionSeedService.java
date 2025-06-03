package com.f1.seasonchampions.service.seed.seasonchampion;

import com.f1.seasonchampions.model.Constructor;
import com.f1.seasonchampions.model.Driver;
import com.f1.seasonchampions.model.SeasonChampion;
import com.f1.seasonchampions.model.SeasonRangeRequest;
import com.f1.seasonchampions.repository.ConstructorRepository;
import com.f1.seasonchampions.repository.DriverRepository;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalSeasonChampionSeedService implements SeasonChampionSeedService {

  private final SeasonChampionRepository seasonChampionRepository;
  private final ConstructorRepository constructorRepository;
  private final DriverRepository driverRepository;

  @Override
  @Transactional(readOnly = true)
  public List<SeasonChampion> getSeasonChampions(final SeasonRangeRequest request) {
    log.info(
        "Fetching season champions from local database: {} to {}",
        request.getStartYear(),
        request.getEndYear());

    return this.seasonChampionRepository.findBySeasonBetweenOrderBySeason(
        String.valueOf(request.getStartYear()), String.valueOf(request.getEndYear()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<SeasonChampion> getAllSeasonChampions() {
    log.info("Fetching all season champions from local database");
    return this.seasonChampionRepository.findAll();
  }

  @Override
  @Transactional
  public SeasonChampion saveChampion(final SeasonChampion champion) {
    log.debug("Saving champion to database: {}", champion);

    // Look up existing constructor before saving
    if (champion.getConstructor() != null) {
      final Optional<Constructor> existingConstructor =
          this.constructorRepository.findByConstructorId(
              champion.getConstructor().getConstructorId());

      if (existingConstructor.isPresent()) {
        champion.setConstructor(existingConstructor.get());
      } else {
        // Save the new constructor first
        final Constructor savedConstructor =
            this.constructorRepository.save(champion.getConstructor());
        champion.setConstructor(savedConstructor);
      }
    }

    // Look up existing driver before saving
    if (champion.getDriver() != null) {
      final Optional<Driver> existingDriver =
          this.driverRepository.findById(champion.getDriver().getDriverId());

      if (existingDriver.isPresent()) {
        champion.setDriver(existingDriver.get());
      } else {
        // Save the new driver first
        final Driver savedDriver = this.driverRepository.save(champion.getDriver());
        champion.setDriver(savedDriver);
      }
    }

    return this.seasonChampionRepository.save(champion);
  }

  public boolean hasCompleteDataForRange(final SeasonRangeRequest request) {
    final List<SeasonChampion> existingChampions = this.getSeasonChampions(request);
    return existingChampions.size() == (request.getEndYear() - request.getStartYear() + 1);
  }
}
