package com.f1.seasonchampions.repository;

import com.f1.seasonchampions.dto.SeasonChampionListItem;
import com.f1.seasonchampions.model.SeasonChampion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonChampionRepository extends JpaRepository<SeasonChampion, String> {
  List<SeasonChampion> findBySeasonBetweenOrderBySeason(String startYear, String endYear);

  @Query(
    value = """
        SELECT sc.season AS season,
               sc.driver_id AS driver,
               sc.constructor_id AS constructor
        FROM season_champions sc
        ORDER BY sc.season
      """,
    nativeQuery = true
  )
  List<SeasonChampionListItem> findAllSeasonChampionListItems();
}
