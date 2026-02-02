package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Taux;
import com.banque.abc.tpe.entity.enums.StatutTaux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TauxRepository extends JpaRepository<Taux, Long> {
    
    List<Taux> findByCommercantId(Long commercantId);
    
    List<Taux> findByStatut(StatutTaux statut);
    
    List<Taux> findByInputerId(Long inputerId);
    
    List<Taux> findByAuthorizerId(Long authorizerId);
    
    @Query("SELECT t FROM Taux t WHERE t.commercant.id = :commercantId AND t.actif = true")
    Optional<Taux> findActiveTauxByCommercant(Long commercantId);
    
    @Query("SELECT t FROM Taux t WHERE t.statut = 'EN_ATTENTE_VALIDATION'")
    List<Taux> findTauxEnAttenteValidation();
    
    @Query("SELECT COUNT(t) FROM Taux t WHERE t.statut = 'EN_ATTENTE_VALIDATION'")
    Long countTauxEnAttenteValidation();
}
