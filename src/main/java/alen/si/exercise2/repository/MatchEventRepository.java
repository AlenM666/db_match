package alen.si.exercise2.repository;

import alen.si.exercise2.model.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
  @Query("SELECT MIN(m.dateInsert) FROM MatchEvent m")
  Instant findMinDateInsert();

  @Query("SELECT MAX(m.dateInsert) FROM MatchEvent m")
  Instant findMaxDateInsert();
}