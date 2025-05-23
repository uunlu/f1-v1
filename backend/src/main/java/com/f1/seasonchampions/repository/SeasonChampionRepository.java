package com.f1.seasonchampions.repository;

import com.f1.seasonchampions.model.SeasonChampion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonChampionRepository extends JpaRepository<SeasonChampion, String> {
  List<SeasonChampion> findBySeasonBetweenOrderBySeason(String startYear, String endYear);
}
