package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.TPEImportRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TPEImportRecordRepository extends JpaRepository<TPEImportRecord, Long> {

    Optional<TPEImportRecord> findFirstByNAffiliationOrderByCreatedDateDesc(String nAffiliation);

    boolean existsByNAffiliation(String nAffiliation);
}