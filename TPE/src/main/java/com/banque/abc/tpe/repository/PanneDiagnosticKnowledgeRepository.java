package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.PanneDiagnosticKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PanneDiagnosticKnowledgeRepository extends JpaRepository<PanneDiagnosticKnowledge, Long> {

    List<PanneDiagnosticKnowledge> findByActifTrueOrderByPrioriteDescLastModifiedDateDesc();

    Optional<PanneDiagnosticKnowledge> findByTitre(String titre);
}
