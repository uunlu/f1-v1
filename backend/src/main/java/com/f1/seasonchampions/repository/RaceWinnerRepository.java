package com.f1.seasonchampions.repository;

import com.f1.seasonchampions.dto.RaceWinnerListItem;
import com.f1.seasonchampions.model.RaceWinner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceWinnerRepository extends JpaRepository<RaceWinner, Long> {

  @Query(
      "SELECT rw FROM RaceWinner "
          + "rw WHERE rw.season = :season AND (:round IS NULL OR rw.round = :round) ORDER BY rw.round")
  List<RaceWinner> findBySeasonAndOptionalRound(
    @Param("season") String season, @Param("round") String round);

  @Query(
    """
SELECT distinct sc.season as seasonName,
       CASE WHEN sc.driver.driverId = d.driverId THEN true ELSE false END AS champion,
       d as driver,
       sc.driver.driverId AS seasonDriverId
FROM SeasonChampion sc
JOIN sc.driver d
JOIN RaceWinner rw ON rw.driver.driverId = d.driverId
WHERE sc.season = :season
ORDER BY sc.season
""")
  List<RaceWinnerListItem> findBySeasonAndOptionalRound2(
    @Param("season") String season);
}
