package com.banque.abc.tpe.config;

import com.banque.abc.tpe.entity.Role;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.RoleType;
import com.banque.abc.tpe.repository.RoleRepository;
import com.banque.abc.tpe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Créer les rôles s'ils n'existent pas
            createRoleIfNotExists(roleRepository, RoleType.ROLE_ADMIN, "Administrateur système");
            createRoleIfNotExists(roleRepository, RoleType.ROLE_MONETIQUE, "Service Monétique");
            createRoleIfNotExists(roleRepository, RoleType.ROLE_AGENCE, "Agence bancaire");
            createRoleIfNotExists(roleRepository, RoleType.ROLE_INPUTER, "Saisie des taux (Monétique)");
            createRoleIfNotExists(roleRepository, RoleType.ROLE_AUTHORIZER, "Validation des taux (Monétique)");

            // Créer un utilisateur admin par défaut
            if (!userRepository.existsByUsername("admin")) {
                Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Rôle ADMIN non trouvé"));

                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);

                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin@123"))
                        .nom("Admin")
                        .prenom("System")
                        .email("admin@banque.com")
                        .actif(true)
                        .accountLocked(false)
                        .failedLoginAttempts(0)
                        .roles(roles)
                        .build();

                userRepository.save(admin);
                System.out.println("Utilisateur admin créé: username=admin, password=Admin@123");
            }

            // Créer un utilisateur Monétique
            if (!userRepository.existsByUsername("monetique")) {
                Role monetiqueRole = roleRepository.findByName(RoleType.ROLE_MONETIQUE)
                        .orElseThrow(() -> new RuntimeException("Rôle MONETIQUE non trouvé"));

                Set<Role> roles = new HashSet<>();
                roles.add(monetiqueRole);

                User monetique = User.builder()
                        .username("monetique")
                        .password(passwordEncoder.encode("Monetique@123"))
                        .nom("Service")
                        .prenom("Monétique")
                        .email("monetique@banque.com")
                        .actif(true)
                        .accountLocked(false)
                        .failedLoginAttempts(0)
                        .roles(roles)
                        .build();

                userRepository.save(monetique);
                System.out.println("Utilisateur monétique créé: username=monetique, password=Monetique@123");
            }

            // Créer un utilisateur Agence
            if (!userRepository.existsByUsername("agence")) {
                Role agenceRole = roleRepository.findByName(RoleType.ROLE_AGENCE)
                        .orElseThrow(() -> new RuntimeException("Rôle AGENCE non trouvé"));

                Set<Role> roles = new HashSet<>();
                roles.add(agenceRole);

                User agence = User.builder()
                        .username("agence")
                        .password(passwordEncoder.encode("Agence@123"))
                        .nom("Agence")
                        .prenom("Centrale")
                        .email("agence@banque.com")
                        .codeAgence("001")
                        .actif(true)
                        .accountLocked(false)
                        .failedLoginAttempts(0)
                        .roles(roles)
                        .build();

                userRepository.save(agence);
                System.out.println("Utilisateur agence créé: username=agence, password=Agence@123");
            }

            // Créer un Inputer
            if (!userRepository.existsByUsername("inputer")) {
                Role inputerRole = roleRepository.findByName(RoleType.ROLE_INPUTER)
                        .orElseThrow(() -> new RuntimeException("Rôle INPUTER non trouvé"));

                Set<Role> roles = new HashSet<>();
                roles.add(inputerRole);

                User inputer = User.builder()
                        .username("inputer")
                        .password(passwordEncoder.encode("Inputer@123"))
                        .nom("Inputer")
                        .prenom("Taux")
                        .email("inputer@banque.com")
                        .actif(true)
                        .accountLocked(false)
                        .failedLoginAttempts(0)
                        .roles(roles)
                        .build();

                userRepository.save(inputer);
                System.out.println("Utilisateur inputer créé: username=inputer, password=Inputer@123");
            }

            // Créer un Authorizer
            if (!userRepository.existsByUsername("authorizer")) {
                Role authorizerRole = roleRepository.findByName(RoleType.ROLE_AUTHORIZER)
                        .orElseThrow(() -> new RuntimeException("Rôle AUTHORIZER non trouvé"));

                Set<Role> roles = new HashSet<>();
                roles.add(authorizerRole);

                User authorizer = User.builder()
                        .username("authorizer")
                        .password(passwordEncoder.encode("Authorizer@123"))
                        .nom("Authorizer")
                        .prenom("Taux")
                        .email("authorizer@banque.com")
                        .actif(true)
                        .accountLocked(false)
                        .failedLoginAttempts(0)
                        .roles(roles)
                        .build();

                userRepository.save(authorizer);
                System.out.println("Utilisateur authorizer créé: username=authorizer, password=Authorizer@123");
            }

            System.out.println("Initialisation des données terminée !");
        };
    }

    private void createRoleIfNotExists(RoleRepository roleRepository, RoleType roleType, String description) {
        if (roleRepository.findByName(roleType).isEmpty()) {
            Role role = Role.builder()
                    .name(roleType)
                    .description(description)
                    .build();
            roleRepository.save(role);
            System.out.println("Rôle créé: " + roleType);
        }
    }
}
