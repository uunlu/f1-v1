package com.f1.seasonchampions.repository;

import com.f1.seasonchampions.model.SeasonChampion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonChampionRepository extends JpaRepository<SeasonChampion, String> {
    List<SeasonChampion> findBySeasonBetweenOrderBySeason(String startYear, String endYear);
}
