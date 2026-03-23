package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.TPEPostingComp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TPEPostingCompRepository extends JpaRepository<TPEPostingComp, Long> {
    
    /**
     * Trouve toutes les écritures pour une date de session donnée
     * @param sessionDate Date de session au format String (yyyyMMdd)
     * @return Liste des écritures
     */
    List<TPEPostingComp> findBySessionDate(String sessionDate);
}
