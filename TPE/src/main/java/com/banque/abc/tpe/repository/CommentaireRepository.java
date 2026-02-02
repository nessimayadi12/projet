package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    
    List<Commentaire> findByDemandeIdOrderByCreatedDateDesc(Long demandeId);
    
    List<Commentaire> findByAuteurId(Long auteurId);
}
