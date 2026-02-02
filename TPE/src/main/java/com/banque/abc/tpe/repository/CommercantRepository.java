package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.enums.StatutCommercant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommercantRepository extends JpaRepository<Commercant, Long>, JpaSpecificationExecutor<Commercant> {
    
    Optional<Commercant> findByEmail(String email);
    
    Optional<Commercant> findByRaisonSociale(String raisonSociale);
    
    Optional<Commercant> findByNumeroCompte(String numeroCompte);
    
    List<Commercant> findByStatut(StatutCommercant statut);
    
    List<Commercant> findByCodeAgence(String codeAgence);
    
    @Query("SELECT c FROM Commercant c WHERE c.raisonSociale LIKE %:search% OR c.email LIKE %:search%")
    List<Commercant> searchByRaisonSocialeOrEmail(String search);
    
    @Query("SELECT COUNT(c) FROM Commercant c WHERE c.statut = 'ACTIF'")
    Long countCommercantsByStatutActif(boolean actif);
    
    @Query("SELECT COUNT(c) FROM Commercant c WHERE c.createdDate >= :debut")
    Long countNewCommercantsCeMois(LocalDateTime debut);
    
    boolean existsByEmail(String email);
    
    boolean existsByNumeroCompte(String numeroCompte);
}
