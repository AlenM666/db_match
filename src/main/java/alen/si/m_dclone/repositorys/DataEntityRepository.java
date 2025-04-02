package alen.si.m_dclone.repositorys;

import alen.si.m_dclone.entity.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface DataEntityRepository extends JpaRepository<DataEntity, Long> {

    @Query("SELECT MIN(d.dateInsert) FROM DataEntity d")
    Instant findMinDateInsert();

    @Query("SELECT MAX(d.dateInsert) FROM DataEntity d")
    Instant findMaxDateInsert();
}