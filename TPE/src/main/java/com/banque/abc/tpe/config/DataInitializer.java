package com.banque.abc.tpe.config;

import com.banque.abc.tpe.entity.Role;
import com.banque.abc.tpe.entity.Screen;
import com.banque.abc.tpe.entity.ScreenRole;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.RoleType;
import com.banque.abc.tpe.repository.RoleRepository;
import com.banque.abc.tpe.repository.ScreenRepository;
import com.banque.abc.tpe.repository.ScreenRoleRepository;
import com.banque.abc.tpe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                      UserRepository userRepository,
                                                                          ScreenRepository screenRepository,
                                                                          ScreenRoleRepository screenRoleRepository,
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

                        // Créer les écrans utilisés par le frontend
                        seedScreensAndPermissions(roleRepository, screenRepository, screenRoleRepository);

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

    private void seedScreensAndPermissions(RoleRepository roleRepository,
                                           ScreenRepository screenRepository,
                                           ScreenRoleRepository screenRoleRepository) {
        Role admin = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Rôle ADMIN non trouvé"));
        Role monetique = roleRepository.findByName(RoleType.ROLE_MONETIQUE)
                .orElseThrow(() -> new RuntimeException("Rôle MONETIQUE non trouvé"));
        Role agence = roleRepository.findByName(RoleType.ROLE_AGENCE)
                .orElseThrow(() -> new RuntimeException("Rôle AGENCE non trouvé"));
        Role inputer = roleRepository.findByName(RoleType.ROLE_INPUTER)
                .orElseThrow(() -> new RuntimeException("Rôle INPUTER non trouvé"));
        Role authorizer = roleRepository.findByName(RoleType.ROLE_AUTHORIZER)
                .orElseThrow(() -> new RuntimeException("Rôle AUTHORIZER non trouvé"));

        List<Screen> screens = List.of(
                createScreenIfNotExists(screenRepository, "DASHBOARD", "Dashboard", "/dashboard", "dashboard", 1),
                createScreenIfNotExists(screenRepository, "DASHBOARD_TPE", "Dashboard TPE", "/dashboard/tpe", "point_of_sale", 2),
                createScreenIfNotExists(screenRepository, "DASHBOARD_DEMANDES", "Dashboard Demandes", "/dashboard/demandes", "assignment", 3),
                createScreenIfNotExists(screenRepository, "DASHBOARD_PANNES", "Dashboard Pannes", "/dashboard/pannes", "build", 4),
                createScreenIfNotExists(screenRepository, "PROFIL_UTILISATEUR", "Profil Utilisateur", "/user-profile", "person", 5),

                createScreenIfNotExists(screenRepository, "LISTE_TPE", "Liste TPE", "/tpe", "credit_card", 10),
                createScreenIfNotExists(screenRepository, "CREER_TPE", "Créer TPE", "/tpe/new", "add", 11),
                createScreenIfNotExists(screenRepository, "MODIFIER_TPE", "Modifier TPE", "/tpe/:id/edit", "edit", 12),
                createScreenIfNotExists(screenRepository, "DETAIL_TPE", "Détail TPE", "/tpe/:id", "visibility", 13),

                createScreenIfNotExists(screenRepository, "LISTE_COMMERCANTS", "Liste Commerçants", "/commercants", "store", 20),
                createScreenIfNotExists(screenRepository, "CREER_COMMERCANT", "Créer Commerçant", "/commercants/new", "add_business", 21),
                createScreenIfNotExists(screenRepository, "MODIFIER_COMMERCANT", "Modifier Commerçant", "/commercants/:id/edit", "edit", 22),
                createScreenIfNotExists(screenRepository, "DETAIL_COMMERCANT", "Détail Commerçant", "/commercants/:id", "visibility", 23),

                createScreenIfNotExists(screenRepository, "LISTE_DEMANDES", "Liste Demandes", "/demandes", "list", 30),
                createScreenIfNotExists(screenRepository, "CREER_DEMANDE", "Créer Demande", "/demandes/new", "note_add", 31),
                createScreenIfNotExists(screenRepository, "MODIFIER_DEMANDE", "Modifier Demande", "/demandes/:id/edit", "edit", 32),
                createScreenIfNotExists(screenRepository, "AFFECTER_TPE", "Affecter TPE", "/demandes/:id/affecter", "assignment_turned_in", 33),
                createScreenIfNotExists(screenRepository, "DETAIL_DEMANDE", "Détail Demande", "/demandes/:id", "visibility", 34),

                createScreenIfNotExists(screenRepository, "LISTE_PANNES", "Liste Pannes", "/pannes", "build", 40),
                createScreenIfNotExists(screenRepository, "GESTION_PERMISSIONS", "Gestion Permissions", "/admin/screens", "security", 50)
        );

        for (Screen screen : screens) {
            // ADMIN: accès complet
            upsertScreenRole(screenRoleRepository, screen, admin, true, true, true, true, true);

            // MONETIQUE: accès large opérationnel
            boolean monetiqueCanView = true;
            boolean monetiqueCanCreate = !screen.getCode().startsWith("DASHBOARD") && !"PROFIL_UTILISATEUR".equals(screen.getCode());
            boolean monetiqueCanEdit = monetiqueCanCreate;
            boolean monetiqueCanDelete = screen.getCode().startsWith("MODIFIER_");
            boolean monetiqueCanExport = screen.getCode().startsWith("LISTE_") || screen.getCode().startsWith("DASHBOARD");
            upsertScreenRole(screenRoleRepository, screen, monetique,
                    monetiqueCanView, monetiqueCanCreate, monetiqueCanEdit, monetiqueCanDelete, monetiqueCanExport);

            // AGENCE: accès principalement demandes/commerçants/tpe consultation
            boolean agenceCanView = switch (screen.getCode()) {
                case "PROFIL_UTILISATEUR", "LISTE_TPE", "DETAIL_TPE", "LISTE_COMMERCANTS", "DETAIL_COMMERCANT",
                        "LISTE_DEMANDES", "CREER_DEMANDE", "MODIFIER_DEMANDE", "DETAIL_DEMANDE", "LISTE_PANNES" -> true;
                default -> false;
            };
            boolean agenceCanCreate = "CREER_DEMANDE".equals(screen.getCode());
            boolean agenceCanEdit = "MODIFIER_DEMANDE".equals(screen.getCode());
            boolean agenceCanDelete = false;
            boolean agenceCanExport = "LISTE_DEMANDES".equals(screen.getCode()) || "LISTE_TPE".equals(screen.getCode()) || "LISTE_COMMERCANTS".equals(screen.getCode());
            upsertScreenRole(screenRoleRepository, screen, agence,
                    agenceCanView, agenceCanCreate, agenceCanEdit, agenceCanDelete, agenceCanExport);

            // INPUTER / AUTHORIZER: au minimum profil utilisateur
            boolean minimalView = "PROFIL_UTILISATEUR".equals(screen.getCode());
            upsertScreenRole(screenRoleRepository, screen, inputer, minimalView, false, false, false, false);
            upsertScreenRole(screenRoleRepository, screen, authorizer, minimalView, false, false, false, false);
        }
    }

    private Screen createScreenIfNotExists(ScreenRepository screenRepository,
                                           String code,
                                           String libelle,
                                           String route,
                                           String icon,
                                           Integer ordre) {
        return screenRepository.findByCode(code).orElseGet(() -> {
            Screen screen = Screen.builder()
                    .code(code)
                    .libelle(libelle)
                    .description(libelle)
                    .route(route)
                    .icon(icon)
                    .ordre(ordre)
                    .actif(true)
                    .build();
            Screen saved = screenRepository.save(screen);
            System.out.println("Screen créé: " + code);
            return saved;
        });
    }

    private void upsertScreenRole(ScreenRoleRepository screenRoleRepository,
                                  Screen screen,
                                  Role role,
                                  boolean canView,
                                  boolean canCreate,
                                  boolean canEdit,
                                  boolean canDelete,
                                  boolean canExport) {
        ScreenRole screenRole = screenRoleRepository.findByScreenIdAndRoleId(screen.getId(), role.getId())
                .orElseGet(() -> ScreenRole.builder()
                        .screen(screen)
                        .role(role)
                        .build());

        screenRole.setCanView(canView);
        screenRole.setCanCreate(canCreate);
        screenRole.setCanEdit(canEdit);
        screenRole.setCanDelete(canDelete);
        screenRole.setCanExport(canExport);
        screenRoleRepository.save(screenRole);
    }
}
