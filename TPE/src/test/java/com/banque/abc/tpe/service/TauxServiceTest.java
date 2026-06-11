package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.taux.TauxRequest;
import com.banque.abc.tpe.dto.taux.TauxResponse;
import com.banque.abc.tpe.dto.taux.ValiderTauxRequest;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Taux;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.StatutTaux;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.exception.UnauthorizedException;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.TauxRepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests du service Taux - Vérification processus 4 yeux
 * ✅ INPUTER crée et soumet
 * ✅ AUTHORIZER ≠ INPUTER valide/rejette
 * ❌ INPUTER ne peut pas valider ses propres saisies
 */
@DisplayName("Tests Taux - Processus 4 Yeux")
class TauxServiceTest {

    private TauxService tauxService;
    
    @Mock
    private TauxRepository tauxRepository;
    @Mock
    private CommercantRepository commercantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private AuditService auditService;

    private Commercant commercantTest;
    private User inputerAlice;
    private User authorizerBob;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tauxService = new TauxService(
            tauxRepository, 
            commercantRepository, 
            userRepository, 
            modelMapper, 
            auditService
        );

        // Setup données test
        commercantTest = Commercant.builder()
            .raisonSociale("Café Central")
            .build();
        commercantTest.setId(1L);

        inputerAlice = User.builder()
            .username("alice")
            .email("alice@bank.com")
            .nom("Alice")
            .prenom("Inputer")
            .password("secret")
            .build();
        inputerAlice.setId(100L);

        authorizerBob = User.builder()
            .username("bob")
            .email("bob@bank.com")
            .nom("Bob")
            .prenom("Authorizer")
            .password("secret")
            .build();
        authorizerBob.setId(200L);
    }

    /**
     * ✅ TEST 1: INPUTER peut créer un taux
     */
    @Test
    @DisplayName("✅ INPUTER peut créer nouveau taux (statut: BROUILLON)")
    void testInputerCanCreateTaux() {
        // Setup: Alice est INPUTER
        mockSecurityContext(inputerAlice.getId(), "ROLE_MONETIQUE");

        when(userRepository.findById(inputerAlice.getId())).thenReturn(Optional.of(inputerAlice));
        when(commercantRepository.findById(commercantTest.getId())).thenReturn(Optional.of(commercantTest));
        when(tauxRepository.findActiveTauxByCommercant(commercantTest.getId())).thenReturn(Optional.empty());
        when(tauxRepository.save(any())).thenAnswer(invocation -> {
            Taux taux = invocation.getArgument(0);
            taux.setId(999L);
            return taux;
        });
        when(modelMapper.map(any(), any())).thenAnswer(invocation -> {
            Taux taux = invocation.getArgument(0);
            TauxResponse response = new TauxResponse();
            response.setId(taux.getId());
            response.setStatut(taux.getStatut());
            return response;
        });

        TauxRequest request = new TauxRequest();
        request.setCommercantId(commercantTest.getId());
        request.setNouveauTauxCommission(1.5);
        request.setNouveauTauxCommissionInter(0.8);

        // Action
        TauxResponse response = tauxService.createTaux(request);

        // Assertions
        assertNotNull(response);
        assertEquals(StatutTaux.BROUILLON, response.getStatut());
        assertEquals(999L, response.getId());
        
        // Vérifier que le taux a été sauvegardé
        ArgumentCaptor<Taux> tauxCaptor = ArgumentCaptor.forClass(Taux.class);
        verify(tauxRepository).save(tauxCaptor.capture());
        assertEquals(StatutTaux.BROUILLON, tauxCaptor.getValue().getStatut());
        assertEquals(inputerAlice.getId(), tauxCaptor.getValue().getInputer().getId());
    }

    /**
     * ✅ TEST 2: AUTHORIZER ≠ INPUTER peut approuver un taux
     */
    @Test
    @DisplayName("✅ AUTHORIZER ≠ INPUTER peut approuver taux")
    void testAuthorizerCanValidateTaux() {
        // Setup: Bob est AUTHORIZER (et ≠ Alice qui a créé)
        mockSecurityContext(authorizerBob.getId(), "ROLE_ADMIN");

        Taux tauxEnAttente = Taux.builder()
                .commercant(commercantTest)
                .inputer(inputerAlice)           // ← Alice (INPUTER)
                .statut(StatutTaux.EN_ATTENTE_VALIDATION)
                .nouveauTauxCommission(1.5)
                .nouveauTauxCommissionInter(0.8)
                .build();
        tauxEnAttente.setId(50L);

        when(tauxRepository.findById(50L)).thenReturn(Optional.of(tauxEnAttente));
        when(userRepository.findById(authorizerBob.getId())).thenReturn(Optional.of(authorizerBob));
        when(tauxRepository.findByCommercantId(commercantTest.getId())).thenReturn(new ArrayList<>());
        when(tauxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(), any())).thenAnswer(invocation -> {
            Taux taux = invocation.getArgument(0);
            TauxResponse response = new TauxResponse();
            response.setId(taux.getId());
            response.setStatut(taux.getStatut());
            response.setAuthorizerNom(taux.getAuthorizer().getUsername());
            return response;
        });

        ValiderTauxRequest request = new ValiderTauxRequest();
        request.setApprouver(true);

        // Action
        TauxResponse response = tauxService.validerTaux(50L, request);

        // Assertions
        assertNotNull(response);
        assertEquals(StatutTaux.VALIDE, response.getStatut());
        assertEquals("bob", response.getAuthorizerNom());
        
        // Vérifier que l'audit a été appelé
        verify(auditService).logAction(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    /**
     * ❌ TEST 3: INPUTER ne peut PAS valider ses propres saisies (RÈGLE 4 YEUX)
     */
    @Test
    @DisplayName("❌ INPUTER ne peut PAS valider ses propres saisies (Règle 4 yeux)")
    void testInputerCannotValidateOwnTaux() {
        // Setup: Alice tente de valider son propre taux
        mockSecurityContext(inputerAlice.getId(), "ROLE_MONETIQUE");

        Taux tauxEnAttente = Taux.builder()
                .commercant(commercantTest)
                .inputer(inputerAlice)           // ← Alice créatrice
                .statut(StatutTaux.EN_ATTENTE_VALIDATION)
                .build();
        tauxEnAttente.setId(50L);

        when(tauxRepository.findById(50L)).thenReturn(Optional.of(tauxEnAttente));
        when(userRepository.findById(inputerAlice.getId())).thenReturn(Optional.of(inputerAlice));

        ValiderTauxRequest request = new ValiderTauxRequest();
        request.setApprouver(true);

        // Action & Assertion
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> tauxService.validerTaux(50L, request),
            "Devrait lever une exception BusinessException"
        );

        assertTrue(
            exception.getMessage().contains("4 yeux"),
            "Le message devrait mentionner la règle 4 yeux"
        );
        assertTrue(
            exception.getMessage().contains("propres saisies"),
            "Le message devrait mentionner les propres saisies"
        );
    }

    /**
     * ✅ TEST 4: AUTHORIZER peut rejeter un taux avec motif
     */
    @Test
    @DisplayName("✅ AUTHORIZER peut rejeter taux avec motif")
    void testAuthorizerCanRejectTaux() {
        // Setup: Bob rejette le taux d'Alice
        mockSecurityContext(authorizerBob.getId(), "ROLE_ADMIN");

        Taux tauxEnAttente = Taux.builder()
                .commercant(commercantTest)
                .inputer(inputerAlice)
                .statut(StatutTaux.EN_ATTENTE_VALIDATION)
                .build();
        tauxEnAttente.setId(50L);

        when(tauxRepository.findById(50L)).thenReturn(Optional.of(tauxEnAttente));
        when(userRepository.findById(authorizerBob.getId())).thenReturn(Optional.of(authorizerBob));
        when(tauxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(), any())).thenAnswer(invocation -> {
            Taux taux = invocation.getArgument(0);
            TauxResponse response = new TauxResponse();
            response.setId(taux.getId());
            response.setStatut(taux.getStatut());
            response.setMotifRejet(taux.getMotifRejet());
            return response;
        });

        ValiderTauxRequest request = new ValiderTauxRequest();
        request.setApprouver(false);
        request.setMotifRejet("Taux trop élevé pour ce secteur");

        // Action
        TauxResponse response = tauxService.validerTaux(50L, request);

        // Assertions
        assertNotNull(response);
        assertEquals(StatutTaux.REJETE, response.getStatut());
        assertEquals("Taux trop élevé pour ce secteur", response.getMotifRejet());
    }

    /**
     * ❌ TEST 5: Utilisateur non MONETIQUE/ADMIN ne peut pas valider
     */
    @Test
    @DisplayName("❌ Utilisateur sans rôle MONETIQUE/ADMIN ne peut pas valider")
    void testNonAuthorizerCannotValidate() {
        // Setup: Alice a un rôle non autorisé pour valider les taux
        mockSecurityContext(inputerAlice.getId(), "ROLE_AGENCE");

        Taux tauxEnAttente = Taux.builder()
                .commercant(commercantTest)
                .inputer(inputerAlice)
                .statut(StatutTaux.EN_ATTENTE_VALIDATION)
                .build();
        tauxEnAttente.setId(50L);

        when(tauxRepository.findById(50L)).thenReturn(Optional.of(tauxEnAttente));

        ValiderTauxRequest request = new ValiderTauxRequest();
        request.setApprouver(true);

        // Action & Assertion
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> tauxService.validerTaux(50L, request),
            "Devrait lever UnauthorizedException"
        );

        assertTrue(
            exception.getMessage().contains("Monetique") && exception.getMessage().contains("administrateur"),
            "Le message devrait mentionner les roles autorises"
        );
    }

    // ====== Helper Methods ======

    /**
     * Mock SecurityContext avec utilisateur et rôle spécifiques
     */
    private void mockSecurityContext(Long userId, String... roles) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        UserPrincipal principal = new UserPrincipal(
            userId,
            "testuser",
            "testuser@bank.com",
            "Test",
            "User",
            "hashedPassword",
            authorities
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}
