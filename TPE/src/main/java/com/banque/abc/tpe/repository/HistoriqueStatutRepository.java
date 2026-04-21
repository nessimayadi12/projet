package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {
    
    List<HistoriqueStatut> findByTpeIdOrderByDateChangementDesc(Long tpeId);
    
    @Query("SELECT h FROM HistoriqueStatut h WHERE h.tpe.id = :tpeId ORDER BY h.dateChangement DESC")
    List<HistoriqueStatut> findHistoriqueByTpe(Long tpeId);

    @Query(value = "SELECT YEAR(h.date_changement) AS annee, MONTH(h.date_changement) AS mois, h.nouveau_statut AS statut, COUNT(*) AS total FROM historique_statuts h WHERE h.date_changement >= :debut GROUP BY YEAR(h.date_changement), MONTH(h.date_changement), h.nouveau_statut ORDER BY annee, mois, statut", nativeQuery = true)
    List<Object[]> countChangesByMonthSince(LocalDateTime debut);
}
