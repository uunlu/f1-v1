package com.f1.seasonchampions.repository;

import com.f1.seasonchampions.model.RaceWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceWinnerRepository extends JpaRepository<RaceWinner, Long> {
    List<RaceWinner> findBySeasonOrderByRound(String season);
}
