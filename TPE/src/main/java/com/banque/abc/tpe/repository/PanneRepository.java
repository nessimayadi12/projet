package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.enums.StatutPanne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PanneRepository extends JpaRepository<Panne, Long>, JpaSpecificationExecutor<Panne> {
    
    Optional<Panne> findByReference(String reference);
    
    List<Panne> findByTpeId(Long tpeId);
    
    List<Panne> findByStatut(StatutPanne statut);
    
    List<Panne> findByDeclarantId(Long declarantId);
    
    List<Panne> findByTechnicienId(Long technicienId);
    
    @Query("SELECT p FROM Panne p WHERE p.statut = :statut AND p.dateDeclaration BETWEEN :startDate AND :endDate")
    List<Panne> findByStatutAndDateRange(StatutPanne statut, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Panne p WHERE p.statut IN ('DECLAREE', 'DIAGNOSTIQUEE', 'EN_REPARATION')")
    Long countPannesEnCours();

    @Query("SELECT COUNT(p) FROM Panne p WHERE p.statut = :statut")
    Long countByStatut(StatutPanne statut);
    
    @Query("SELECT p.statut, COUNT(p) FROM Panne p GROUP BY p.statut")
    List<Object[]> countByStatutGrouped();
    
    @Query("SELECT CASE WHEN p.sousGarantie = true THEN 'Garantie' ELSE 'Hors Garantie' END, COUNT(p) FROM Panne p GROUP BY p.sousGarantie")
    List<Object[]> countByTypeGrouped();
    
    @Query("SELECT COUNT(p) FROM Panne p WHERE p.statut = 'RESOLUE' AND p.dateResolution BETWEEN :debut AND :fin")
    Long countPannesResoluesDansLaPeriode(LocalDateTime debut, LocalDateTime fin);
    
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, p.dateDeclaration, p.dateResolution)) FROM Panne p WHERE p.statut = 'RESOLUE' AND p.dateResolution IS NOT NULL")
    Optional<Double> calculateMTTR();
    
    @Query("SELECT COUNT(p) FROM Panne p WHERE p.statut IN ('DECLAREE', 'DIAGNOSTIQUEE', 'EN_REPARATION') AND TIMESTAMPDIFF(HOUR, p.dateDeclaration, CURRENT_TIMESTAMP) > 48")
    Long countPannesDepassantSLA();
    
    @Query("SELECT COUNT(p) FROM Panne p WHERE p.dateDeclaration BETWEEN :debut AND :fin")
    Long countByDateBetween(LocalDateTime debut, LocalDateTime fin);
    
    @Query("SELECT p.statut, COUNT(p) as cnt FROM Panne p WHERE p.dateDeclaration BETWEEN :debut AND :fin GROUP BY p.statut")
    List<Object[]> countByDateBetweenGrouped(LocalDateTime debut, LocalDateTime fin);

    @Query(value = "SELECT DAYOFWEEK(p.date_declaration) AS day_of_week, CASE WHEN HOUR(p.date_declaration) < 6 THEN '00h-06h' WHEN HOUR(p.date_declaration) < 12 THEN '06h-12h' WHEN HOUR(p.date_declaration) < 18 THEN '12h-18h' ELSE '18h-24h' END AS time_slot, COUNT(*) AS total FROM pannes p GROUP BY DAYOFWEEK(p.date_declaration), CASE WHEN HOUR(p.date_declaration) < 6 THEN '00h-06h' WHEN HOUR(p.date_declaration) < 12 THEN '06h-12h' WHEN HOUR(p.date_declaration) < 18 THEN '12h-18h' ELSE '18h-24h' END ORDER BY day_of_week, time_slot", nativeQuery = true)
    List<Object[]> countHeatmapByDayAndPeriod();
    
    @Query("SELECT p.statut, COUNT(p) as frequency FROM Panne p GROUP BY p.statut ORDER BY frequency DESC")
    List<Object[]> findTopPannesByFrequency();
    
    boolean existsByReference(String reference);
}
