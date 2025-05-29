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
    SELECT\s
        rw.season AS seasonName,
        rw.round AS round,
        CASE WHEN sc.driver IS NOT NULL THEN true ELSE false END AS champion,
        d AS driver,
        sc.driver.driverId AS seasonDriverId
    FROM RaceWinner rw
    JOIN rw.driver d
    LEFT JOIN SeasonChampion sc\s
        ON sc.season = rw.season AND sc.driver.driverId = rw.driver.driverId
    WHERE rw.season = :season
    ORDER BY CAST(rw.round AS integer)
""")
  List<RaceWinnerListItem> findBySeasonAndOptionalRound2(@Param("season") String season);

  List<RaceWinner> getRaceWinnerBySeason(String season);
}
