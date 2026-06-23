package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.assistant.AssistantRequestDTO;
import com.banque.abc.tpe.dto.assistant.AssistantResponseDTO;
import com.banque.abc.tpe.service.AssistantMetierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantMetierService assistantMetierService;

    /**
     * Recoit une question libre et retourne la reponse IA basee sur les donnees reelles.
     */
    @PostMapping("/interroger")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<AssistantResponseDTO> interroger(@Valid @RequestBody AssistantRequestDTO request) {
        return ResponseEntity.ok(assistantMetierService.interroger(request.getQuestion()));
    }
}
