package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {
    
    List<HistoriqueStatut> findByTpeIdOrderByDateChangementDesc(Long tpeId);
    
    @Query("SELECT h FROM HistoriqueStatut h WHERE h.tpe.id = :tpeId ORDER BY h.dateChangement DESC")
    List<HistoriqueStatut> findHistoriqueByTpe(Long tpeId);
}
