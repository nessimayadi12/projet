package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long>, JpaSpecificationExecutor<Demande> {
    
    Optional<Demande> findByReference(String reference);

    Optional<Demande> findFirstByReferenceOrderByLastModifiedDateDescIdDesc(String reference);
    
    List<Demande> findByStatut(StatutDemande statut);
    
    List<Demande> findByCommercantId(Long commercantId);
    
    List<Demande> findByDemandeurId(Long demandeurId);
    
    @Query("SELECT d FROM Demande d WHERE d.statut = :statut AND d.createdDate BETWEEN :startDate AND :endDate")
    List<Demande> findByStatutAndDateRange(StatutDemande statut, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.statut = :statut")
    Long countByStatut(StatutDemande statut);

    @Query("SELECT COUNT(d) FROM Demande d WHERE d.statut IN :statuts")
    Long countByStatutIn(@Param("statuts") List<StatutDemande> statuts);

    @Query("SELECT COUNT(d) FROM Demande d WHERE d.statut IN :statuts AND d.dateCloture BETWEEN :debut AND :fin")
    Long countByStatutInAndDateClotureBetween(
            @Param("statuts") List<StatutDemande> statuts,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(d) FROM Demande d WHERE d.statut IN :statuts AND d.createdDate <= :dateLimite")
    Long countPendingOlderThan(
            @Param("statuts") List<StatutDemande> statuts,
            @Param("dateLimite") LocalDateTime dateLimite);

    @Query(value = """
            SELECT COUNT(*)
            FROM demandes d
            WHERE d.statut IN ('AFFECTEE', 'CLOTUREE', 'REJETEE')
              AND COALESCE(d.date_cloture, d.date_validation) IS NOT NULL
            """, nativeQuery = true)
    Long countTerminalWithCompletionDate();

    @Query(value = """
            SELECT COUNT(*)
            FROM demandes d
            WHERE d.statut IN ('AFFECTEE', 'CLOTUREE', 'REJETEE')
              AND COALESCE(d.date_cloture, d.date_validation) IS NOT NULL
              AND TIMESTAMPDIFF(HOUR, d.created_date, COALESCE(d.date_cloture, d.date_validation)) <= :maxHours
            """, nativeQuery = true)
    Long countTerminalWithinSla(@Param("maxHours") long maxHours);
    
    @Query("SELECT d FROM Demande d WHERE d.urgence IN ('HAUTE', 'CRITIQUE') AND d.statut NOT IN ('CLOTUREE', 'REJETEE')")
    List<Demande> findDemandesUrgentes();
    
    @Query("SELECT d.statut, COUNT(d) FROM Demande d GROUP BY d.statut")
    List<Object[]> countByStatutGrouped();
    
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.createdDate BETWEEN :debut AND :fin")
    Long countByDateBetween(LocalDateTime debut, LocalDateTime fin);

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, d.created_date, COALESCE(d.date_validation, d.date_cloture))) FROM demandes d WHERE COALESCE(d.date_validation, d.date_cloture) IS NOT NULL", nativeQuery = true)
    Double calculateAverageTreatmentDelayHours();
    
    boolean existsByReference(String reference);

    @Query("SELECT d FROM Demande d WHERE d.reference LIKE CONCAT(:prefix, '%') AND d.statut = :statut")
    List<Demande> findByReferenceStartingWithAndStatut(String prefix, StatutDemande statut);

    @Query("""
            SELECT d
            FROM Demande d
            WHERE d.statut = :statut
              AND NOT EXISTS (
                  SELECT a.id
                  FROM Affectation a
                  WHERE a.demande = d
                    AND a.actif = true
              )
            ORDER BY d.dateValidation DESC, d.createdDate DESC, d.id DESC
            """)
    List<Demande> findByStatutWithoutActiveAffectation(
            @Param("statut") StatutDemande statut,
            Pageable pageable);

    @Query("""
            SELECT COUNT(d)
            FROM Demande d
            WHERE d.statut = :statut
              AND NOT EXISTS (
                  SELECT a.id
                  FROM Affectation a
                  WHERE a.demande = d
                    AND a.actif = true
              )
            """)
    Long countByStatutWithoutActiveAffectation(@Param("statut") StatutDemande statut);

    @Query(value = """
            SELECT COUNT(DISTINCT COALESCE(NULLIF(d.numero_terminal, ''), NULLIF(d.serie_tpe, ''), d.reference))
            FROM demandes d
            WHERE d.commercant_id = :commercantId
            """, nativeQuery = true)
    Long countDistinctTpeReferencesByCommercantId(@Param("commercantId") Long commercantId);
}
