package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TPERepository extends JpaRepository<TPE, Long>, JpaSpecificationExecutor<TPE> {
    
    Optional<TPE> findByNumeroSerie(String numeroSerie);
    
    Optional<TPE> findByNumeroTerminal(String numeroTerminal);

    Optional<TPE> findByNumeroAffiliation(String numeroAffiliation);
    
    boolean existsByNumeroSerie(String numeroSerie);
    
    boolean existsByNumeroTerminal(String numeroTerminal);

    boolean existsByNumeroAffiliation(String numeroAffiliation);
    
    @Query("SELECT COUNT(t) FROM TPE t WHERE t.numeroTerminal IS NOT NULL AND t.numeroTerminal != ''")
    long countTPEsWithNumeroTerminal();
    
    List<TPE> findByStatut(StatutTPE statut);
    
    List<TPE> findByStatutAndTypeTPE(StatutTPE statut, TypeTPE typeTPE);
    
    List<TPE> findByTypeTPE(TypeTPE typeTPE);
    
    List<TPE> findByCommercantId(Long commercantId);
    
    @Query("SELECT COUNT(t) FROM TPE t WHERE t.statut = :statut")
    Long countByStatut(StatutTPE statut);
    
    @Query("SELECT t FROM TPE t WHERE t.statut = 'DISPONIBLE' AND t.typeTPE = :typeTPE")
    List<TPE> findDisponiblesByType(TypeTPE typeTPE);
    
    @Query("SELECT t.statut, COUNT(t) FROM TPE t GROUP BY t.statut")
    List<Object[]> countByStatutGrouped();
    
    @Query("SELECT t.typeTPE, COUNT(t) FROM TPE t GROUP BY t.typeTPE")
    List<Object[]> countByTypeGrouped();

    @Query("SELECT COALESCE(c.codeAgence, 'INCONNU'), t.statut, COUNT(t) FROM TPE t LEFT JOIN t.commercant c GROUP BY COALESCE(c.codeAgence, 'INCONNU'), t.statut ORDER BY COALESCE(c.codeAgence, 'INCONNU'), t.statut")
    List<Object[]> countByAgenceAndStatutGrouped();

    @Query("SELECT COALESCE(t.marque, 'Inconnu'), COUNT(t) FROM TPE t GROUP BY COALESCE(t.marque, 'Inconnu') ORDER BY COALESCE(t.marque, 'Inconnu')")
    List<Object[]> countByMarqueGrouped();
}
