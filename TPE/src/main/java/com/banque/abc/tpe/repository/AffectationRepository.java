package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    
    List<Affectation> findByTpeId(Long tpeId);
    
    List<Affectation> findByCommercantId(Long commercantId);
    
    List<Affectation> findByCommercantIdOrderByDateAffectationDesc(Long commercantId);
    
    List<Affectation> findByActifTrue();
    
    boolean existsByDemandeIdAndActifTrue(Long demandeId);

    List<Affectation> findByDemandeIdOrderByActifDescDateAffectationDescIdDesc(Long demandeId);
    
    @Query("SELECT a FROM Affectation a WHERE a.tpe.id = :tpeId AND a.actif = true")
    Optional<Affectation> findActiveByTpeId(Long tpeId);

    @Query("SELECT a FROM Affectation a WHERE a.tpe.id = :tpeId AND a.actif = true ORDER BY a.dateAffectation DESC, a.id DESC")
    List<Affectation> findActiveByTpeIdOrderByDateAffectationDescIdDesc(Long tpeId);
    
    @Query("SELECT a FROM Affectation a WHERE a.commercant.id = :commercantId AND a.actif = true")
    List<Affectation> findActiveByCommercantId(Long commercantId);
    
    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.commercant.id = :commercantId AND a.actif = true")
    Long countActiveAffectationsByCommercant(Long commercantId);
    
    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.actif = true")
    Long countAffectationsActives();
    
    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.dateAffectation >= :debut")
    Long countAffectationsCeMois(LocalDateTime debut);
    
    @Query("SELECT c.raisonSociale, COUNT(a) FROM Affectation a JOIN a.commercant c WHERE a.actif = true GROUP BY c.id, c.raisonSociale ORDER BY COUNT(a) DESC")
    List<Object[]> findTop10CommercantsByTPECount();
    
    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.dateAffectation BETWEEN :debut AND :fin")
    Long countByDateBetween(LocalDateTime debut, LocalDateTime fin);
}
