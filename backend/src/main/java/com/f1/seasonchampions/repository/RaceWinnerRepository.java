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
      SELECT
          rw.season AS seasonName,
          rw.round AS round,
          CASE WHEN sc.driver IS NOT NULL THEN true ELSE false END AS champion,
          d AS driver,
          sc.driver.driverId AS seasonDriverId,
          rw.constructor.constructorId AS seasonConstructorId,
          rw.constructor.name AS constructorName
      FROM RaceWinner rw
      JOIN rw.driver d
      LEFT JOIN SeasonChampion sc ON sc.season = rw.season AND sc.driver.driverId = rw.driver.driverId
      WHERE rw.season = :season
      ORDER BY CAST(rw.round AS integer)
      """
  )
  List<RaceWinnerListItem> findRaceWinnersWithConstructors(@Param("season") String season);

  default List<RaceWinner> getRaceWinnerBySeason(String season) {
    return findBySeasonAndOptionalRound(season, null);
  }
}
