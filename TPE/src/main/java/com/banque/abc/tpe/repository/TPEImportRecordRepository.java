package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.TPEImportRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TPEImportRecordRepository extends JpaRepository<TPEImportRecord, Long> {

    @Query("SELECT r FROM TPEImportRecord r WHERE r.nAffiliation = :nAffiliation ORDER BY r.createdDate DESC")
    Optional<TPEImportRecord> findLatestByNAffiliation(@Param("nAffiliation") String nAffiliation);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM TPEImportRecord r WHERE r.nAffiliation = :nAffiliation")
    boolean existsByNAffiliation(@Param("nAffiliation") String nAffiliation);
}