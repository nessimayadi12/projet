package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.auth.LoginRequest;
import com.banque.abc.tpe.dto.auth.LoginResponse;
import com.banque.abc.tpe.dto.auth.RegisterRequest;
import com.banque.abc.tpe.entity.Role;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.RoleType;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.DuplicateResourceException;
import com.banque.abc.tpe.repository.RoleRepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);

            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            auditService.logAction("LOGIN", "User", user.getId().toString(),
                    "Connexion réussie pour " + user.getUsername(), "SUCCESS");

            return new LoginResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);

        } catch (Exception e) {
            auditService.logAction("LOGIN", "User", loginRequest.getUsername(),
                    "Échec de connexion: " + e.getMessage(), "FAILED");
            throw e;
        }
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Le nom d'utilisateur existe déjà");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("L'email existe déjà");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nom(registerRequest.getNom())
                .prenom(registerRequest.getPrenom())
                .email(registerRequest.getEmail())
                .telephone(registerRequest.getTelephone())
                .codeAgence(registerRequest.getCodeAgence())
                .actif(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName(RoleType.ROLE_AGENCE)
                    .orElseThrow(() -> new BusinessException("Rôle AGENCE non trouvé"));
            roles.add(userRole);
        } else {
            for (String roleName : registerRequest.getRoles()) {
                String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                RoleType roleType;
                try {
                    roleType = RoleType.valueOf(normalizedRoleName);
                } catch (IllegalArgumentException e) {
                    throw new BusinessException("Role non autorise: " + roleName);
                }

                Role role = roleRepository.findByName(roleType)
                        .orElseThrow(() -> new BusinessException("Rôle " + roleName + " non trouvé"));
                roles.add(role);
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        auditService.logAction("CREATE", "User", savedUser.getId().toString(),
                "Nouvel utilisateur créé: " + savedUser.getUsername(), "SUCCESS");
    }
}
