package com.f1.seasonchampions.repository;

import com.f1.seasonchampions.model.Constructor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConstructorRepository extends JpaRepository<Constructor, Long> {
  Optional<Constructor> findByConstructorId(String constructorId);
}
