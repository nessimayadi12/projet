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
import org.springframework.data.repository.query.Param;

@Repository
public interface CommercantRepository extends JpaRepository<Commercant, Long>, JpaSpecificationExecutor<Commercant> {
    
    Optional<Commercant> findByEmail(String email);

    Optional<Commercant> findFirstByEmailOrderByLastModifiedDateDescIdDesc(String email);
    
    Optional<Commercant> findByRaisonSociale(String raisonSociale);

    Optional<Commercant> findFirstByRaisonSociale(String raisonSociale);

    Optional<Commercant> findFirstByRaisonSocialeOrderByLastModifiedDateDescIdDesc(String raisonSociale);
    
    Optional<Commercant> findByNumeroCompte(String numeroCompte);

    Optional<Commercant> findByNumeroCompteAndCodeAgence(String numeroCompte, String codeAgence);

    Optional<Commercant> findByRaisonSocialeAndCodeAgence(String raisonSociale, String codeAgence);

        @Query("""
                SELECT c FROM Commercant c
                WHERE c.numeroCompte = :numeroCompte
                    AND c.codeAgence = :codeAgence
                    AND (
                                (:adresse IS NULL AND c.adresse IS NULL)
                                OR LOWER(TRIM(COALESCE(c.adresse, ''))) = LOWER(TRIM(COALESCE(:adresse, '')))
                            )
                """)
        Optional<Commercant> findForImportByCompteAgenceAdresse(@Param("numeroCompte") String numeroCompte,
                                                                                                                         @Param("codeAgence") String codeAgence,
                                                                                                                         @Param("adresse") String adresse);

        @Query("""
                SELECT c FROM Commercant c
                WHERE c.raisonSociale = :raisonSociale
                    AND c.codeAgence = :codeAgence
                    AND (
                                (:adresse IS NULL AND c.adresse IS NULL)
                                OR LOWER(TRIM(COALESCE(c.adresse, ''))) = LOWER(TRIM(COALESCE(:adresse, '')))
                            )
                """)
        Optional<Commercant> findForImportByNomAgenceAdresse(@Param("raisonSociale") String raisonSociale,
                                                                                                                    @Param("codeAgence") String codeAgence,
                                                                                                                    @Param("adresse") String adresse);

        @Query("""
                SELECT c FROM Commercant c
                WHERE LOWER(TRIM(COALESCE(c.raisonSociale, ''))) = LOWER(TRIM(COALESCE(:raisonSociale, '')))
                    AND LOWER(TRIM(COALESCE(c.numeroCompte, ''))) = LOWER(TRIM(COALESCE(:numeroCompte, '')))
                    AND LOWER(TRIM(COALESCE(c.codeAgence, ''))) = LOWER(TRIM(COALESCE(:codeAgence, '')))
                    AND LOWER(TRIM(COALESCE(c.adresse, ''))) = LOWER(TRIM(COALESCE(:adresse, '')))
                ORDER BY c.lastModifiedDate DESC, c.id DESC
                """)
        List<Commercant> findForImportExact(@Param("raisonSociale") String raisonSociale,
                                            @Param("numeroCompte") String numeroCompte,
                                            @Param("codeAgence") String codeAgence,
                                            @Param("adresse") String adresse);
    
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
