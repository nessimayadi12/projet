package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long>, JpaSpecificationExecutor<Demande> {
    
    Optional<Demande> findByReference(String reference);
    
    List<Demande> findByStatut(StatutDemande statut);
    
    List<Demande> findByCommercantId(Long commercantId);
    
    List<Demande> findByDemandeurId(Long demandeurId);
    
    @Query("SELECT d FROM Demande d WHERE d.statut = :statut AND d.createdDate BETWEEN :startDate AND :endDate")
    List<Demande> findByStatutAndDateRange(StatutDemande statut, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.statut = :statut")
    Long countByStatut(StatutDemande statut);
    
    @Query("SELECT d FROM Demande d WHERE d.urgence IN ('HAUTE', 'CRITIQUE') AND d.statut NOT IN ('CLOTUREE', 'REJETEE')")
    List<Demande> findDemandesUrgentes();
    
    @Query("SELECT d.statut, COUNT(d) FROM Demande d GROUP BY d.statut")
    List<Object[]> countByStatutGrouped();
    
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.createdDate BETWEEN :debut AND :fin")
    Long countByDateBetween(LocalDateTime debut, LocalDateTime fin);
    
    boolean existsByReference(String reference);

    @Query("SELECT d FROM Demande d WHERE d.reference LIKE CONCAT(:prefix, '%') AND d.statut = :statut")
    List<Demande> findByReferenceStartingWithAndStatut(String prefix, StatutDemande statut);
}
