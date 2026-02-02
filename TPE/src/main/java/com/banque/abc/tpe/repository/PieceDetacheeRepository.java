package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.PieceDetachee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceDetacheeRepository extends JpaRepository<PieceDetachee, Long> {
    
    List<PieceDetachee> findByPanneId(Long panneId);
}
