package com.f1.seasonchampions.repository;

import com.f1.seasonchampions.model.RaceWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceWinnerRepository extends JpaRepository<RaceWinner, Long> {
  @Query("SELECT rw FROM RaceWinner rw " +
    "WHERE rw.season = :season " +
    "AND (:round IS NULL OR rw.round = :round) " +
    "ORDER BY rw.round")
  List<RaceWinner> findBySeasonAndOptionalRound(@Param("season") String season, @Param("round") String round);
}
