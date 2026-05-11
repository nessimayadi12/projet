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
import org.springframework.jdbc.core.JdbcTemplate;
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
                                      JdbcTemplate jdbcTemplate,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            removeDeprecatedRoles(jdbcTemplate);
            backfillBaseEntityColumns(jdbcTemplate);

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
                Role monetiqueRole = roleRepository.findByName(RoleType.ROLE_MONETIQUE)
                        .orElseThrow(() -> new RuntimeException("Rôle MONETIQUE non trouvé"));
                Role inputerRole = roleRepository.findByName(RoleType.ROLE_INPUTER)
                        .orElseThrow(() -> new RuntimeException("Rôle INPUTER non trouvé"));

                Set<Role> roles = new HashSet<>();
                roles.add(monetiqueRole);
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
            ensureUserHasRoles(jdbcTemplate, "inputer",
                    RoleType.ROLE_MONETIQUE, RoleType.ROLE_INPUTER);

            // Créer un Authorizer
            if (!userRepository.existsByUsername("authorizer")) {
                Role monetiqueRole = roleRepository.findByName(RoleType.ROLE_MONETIQUE)
                        .orElseThrow(() -> new RuntimeException("Rôle MONETIQUE non trouvé"));
                Role authorizerRole = roleRepository.findByName(RoleType.ROLE_AUTHORIZER)
                        .orElseThrow(() -> new RuntimeException("Rôle AUTHORIZER non trouvé"));

                Set<Role> roles = new HashSet<>();
                roles.add(monetiqueRole);
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
            ensureUserHasRoles(jdbcTemplate, "authorizer",
                    RoleType.ROLE_MONETIQUE, RoleType.ROLE_AUTHORIZER);
            ensureMonetiqueForSubRoleUsers(jdbcTemplate);

                        // Créer les écrans utilisés par le frontend
                        seedScreensAndPermissions(roleRepository, screenRepository, screenRoleRepository);

            System.out.println("Initialisation des données terminée !");
        };
    }

    private void removeDeprecatedRoles(JdbcTemplate jdbcTemplate) {
        int screenRolesDeleted = jdbcTemplate.update(
                "DELETE FROM screen_roles WHERE role_id IN (SELECT id FROM roles WHERE name IN (?, ?))",
                "ROLE_TECHNICIEN",
                "ROLE_LOGISTIQUE"
        );
        int userRolesDeleted = jdbcTemplate.update(
                "DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE name IN (?, ?))",
                "ROLE_TECHNICIEN",
                "ROLE_LOGISTIQUE"
        );
        int rolesDeleted = jdbcTemplate.update(
                "DELETE FROM roles WHERE name IN (?, ?)",
                "ROLE_TECHNICIEN",
                "ROLE_LOGISTIQUE"
        );

        if (screenRolesDeleted + userRolesDeleted + rolesDeleted > 0) {
            System.out.println("Roles obsoletes supprimes: TECHNICIEN, LOGISTIQUE");
        }
    }

    private void backfillBaseEntityColumns(JdbcTemplate jdbcTemplate) {
        List<String> baseEntityTables = List.of(
                "users",
                "commercants",
                "demandes",
                "tpes",
                "affectations",
                "pannes",
                "commentaires",
                "pieces_jointes",
                "taux",
                "screens",
                "tpe_import_records",
                "TPE_POSTING_comp"
        );

        for (String table : baseEntityTables) {
            jdbcTemplate.update("UPDATE " + table + " SET version = 0 WHERE version IS NULL");
            jdbcTemplate.update("UPDATE " + table
                    + " SET created_date = COALESCE(created_date, CURRENT_TIMESTAMP), "
                    + "last_modified_date = COALESCE(last_modified_date, CURRENT_TIMESTAMP) "
                    + "WHERE created_date IS NULL OR last_modified_date IS NULL");
        }
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

    private void ensureUserHasRoles(JdbcTemplate jdbcTemplate,
                                    String username,
                                    RoleType... requiredRoles) {
        for (RoleType requiredRole : requiredRoles) {
            int inserted = jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_id) "
                            + "SELECT u.id, r.id FROM users u "
                            + "JOIN roles r ON r.name = ? "
                            + "WHERE u.username = ? "
                            + "AND NOT EXISTS ("
                            + "SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id"
                            + ")",
                    requiredRole.name(),
                    username
            );

            if (inserted > 0) {
                System.out.println("Role " + requiredRole + " synchronise pour " + username);
            }
        }
    }

    private void ensureMonetiqueForSubRoleUsers(JdbcTemplate jdbcTemplate) {
        int inserted = jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) "
                        + "SELECT DISTINCT u.id, monetique.id FROM users u "
                        + "JOIN user_roles ur_sub ON ur_sub.user_id = u.id "
                        + "JOIN roles sub_role ON sub_role.id = ur_sub.role_id "
                        + "JOIN roles monetique ON monetique.name = ? "
                        + "WHERE sub_role.name IN (?, ?) "
                        + "AND NOT EXISTS ("
                        + "SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = monetique.id"
                        + ")",
                RoleType.ROLE_MONETIQUE.name(),
                RoleType.ROLE_INPUTER.name(),
                RoleType.ROLE_AUTHORIZER.name()
        );

        if (inserted > 0) {
            System.out.println("Role MONETIQUE ajoute pour " + inserted + " utilisateur(s) inputer/authorizer");
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

                createScreenIfNotExists(screenRepository, "GESTION_TAUX", "Gestion Taux", "/taux", "percent", 14),

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
                createScreenIfNotExists(screenRepository, "UPLOAD_FICHIER_BANCAIRE", "Upload Transactions", "/file-upload", "cloud_upload", 45),
                createScreenIfNotExists(screenRepository, "GESTION_PERMISSIONS", "Gestion Permissions", "/admin/screens", "security", 50)
        );

        for (Screen screen : screens) {
            // ADMIN: accès complet
            upsertScreenRole(screenRoleRepository, screenRepository, roleRepository,
                    screen, admin, true, true, true, true, true);

            // MONETIQUE: accès large opérationnel
            boolean isPermissionAdminScreen = "GESTION_PERMISSIONS".equals(screen.getCode());
            boolean monetiqueCanView = !isPermissionAdminScreen;
            boolean monetiqueCanCreate = monetiqueCanView
                    && !screen.getCode().startsWith("DASHBOARD")
                    && !"PROFIL_UTILISATEUR".equals(screen.getCode());
            boolean monetiqueCanEdit = monetiqueCanCreate;
            boolean monetiqueCanDelete = screen.getCode().startsWith("MODIFIER_");
            boolean monetiqueCanExport = monetiqueCanView
                    && (screen.getCode().startsWith("LISTE_") || screen.getCode().startsWith("DASHBOARD"));
            upsertScreenRole(screenRoleRepository, screenRepository, roleRepository,
                    screen, monetique,
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
            upsertScreenRole(screenRoleRepository, screenRepository, roleRepository,
                    screen, agence,
                    agenceCanView, agenceCanCreate, agenceCanEdit, agenceCanDelete, agenceCanExport);

                        // INPUTER / AUTHORIZER: profil + gestion des taux
                        boolean tauxView = "PROFIL_UTILISATEUR".equals(screen.getCode()) || "GESTION_TAUX".equals(screen.getCode());
                        upsertScreenRole(screenRoleRepository, screenRepository, roleRepository,
                                screen, inputer, tauxView, false, false, false, false);
                        upsertScreenRole(screenRoleRepository, screenRepository, roleRepository,
                                screen, authorizer, tauxView, false, false, false, false);
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
                                                                  ScreenRepository screenRepository,
                                                                  RoleRepository roleRepository,
                                                                  Screen screen,
                                                                  Role role,
                                  boolean canView,
                                  boolean canCreate,
                                  boolean canEdit,
                                  boolean canDelete,
                                  boolean canExport) {
                Screen screenRef = screenRepository.getReferenceById(screen.getId());
                Role roleRef = roleRepository.getReferenceById(role.getId());
        ScreenRole screenRole = screenRoleRepository.findByScreenIdAndRoleId(screen.getId(), role.getId())
                .orElseGet(() -> ScreenRole.builder()
                                                .screen(screenRef)
                                                .role(roleRef)
                        .build());

                screenRole.setScreen(screenRef);
                screenRole.setRole(roleRef);
        screenRole.setCanView(canView);
        screenRole.setCanCreate(canCreate);
        screenRole.setCanEdit(canEdit);
        screenRole.setCanDelete(canDelete);
        screenRole.setCanExport(canExport);
        screenRoleRepository.save(screenRole);
    }
}
