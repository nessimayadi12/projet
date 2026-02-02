package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {
    
    List<PieceJointe> findByDemandeId(Long demandeId);
}
