package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.assistant.AssistantRequestDTO;
import com.banque.abc.tpe.dto.assistant.AssistantResponseDTO;
import com.banque.abc.tpe.service.AssistantMetierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/assistant-metier", "/api/assistant-ia"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AssistantMetierController {

    private final AssistantMetierService assistantMetierService;

    /**
     * Alias historique vers le nouvel assistant dynamique.
     */
    @PostMapping("/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<AssistantResponseDTO> poserQuestion(@Valid @RequestBody AssistantRequestDTO request) {
        return ResponseEntity.ok(assistantMetierService.interroger(request.getQuestion()));
    }
}
