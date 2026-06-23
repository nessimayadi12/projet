package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.ScreenDTO;
import com.banque.abc.tpe.dto.ScreenPermissionsDTO;
import com.banque.abc.tpe.dto.ScreenRoleDTO;
import com.banque.abc.tpe.dto.UserScreensDTO;
import com.banque.abc.tpe.entity.Role;
import com.banque.abc.tpe.entity.Screen;
import com.banque.abc.tpe.entity.ScreenRole;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.RoleRepository;
import com.banque.abc.tpe.repository.ScreenRepository;
import com.banque.abc.tpe.repository.ScreenRoleRepository;
import com.banque.abc.tpe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final ScreenRoleRepository screenRoleRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ScreenDTO> getAllScreens() {
        return screenRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScreenDTO> getActiveScreens() {
        return screenRepository.findByActifTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScreenDTO getScreenById(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen non trouvé avec l'id: " + id));
        return mapToDTO(screen);
    }

    @Transactional(readOnly = true)
    public ScreenDTO getScreenByCode(String code) {
        Screen screen = screenRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Screen non trouvé avec le code: " + code));
        return mapToDTO(screen);
    }

    @Transactional(readOnly = true)
    public UserScreensDTO getScreensForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));

        Map<String, ScreenDTO> screensByCode = new LinkedHashMap<>();
        
        // Récupérer les screens pour tous les rôles de l'utilisateur
        for (Role role : user.getRoles()) {
            List<Screen> roleScreens = screenRepository.findByRoleId(role.getId());
            for (Screen screen : roleScreens) {
                
                // Ajouter les permissions spécifiques pour ce rôle
                ScreenRole screenRole = screenRoleRepository.findByScreenIdAndRoleId(screen.getId(), role.getId())
                        .orElse(null);
                
                if (screenRole == null) {
                    continue;
                }
                
                // Éviter les doublons
                ScreenDTO screenDTO = screensByCode.computeIfAbsent(screen.getCode(), code -> mapToDTO(screen));
                screenDTO.setPermissions(mergePermissions(screenDTO.getPermissions(), screenRole));
            }
        }

        List<ScreenDTO> screens = screensByCode.values().stream()
                .filter(screen -> screen.getPermissions() != null && Boolean.TRUE.equals(screen.getPermissions().getCanView()))
                .collect(Collectors.toList());

        // Trier par ordre
        screens.sort((s1, s2) -> {
            int order1 = s1.getOrdre() != null ? s1.getOrdre() : Integer.MAX_VALUE;
            int order2 = s2.getOrdre() != null ? s2.getOrdre() : Integer.MAX_VALUE;
            return Integer.compare(order1, order2);
        });

        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().toString())
                .sorted()
                .collect(Collectors.toList());

        return UserScreensDTO.builder()
                .username(user.getUsername())
                .role(roleNames.isEmpty() ? null : roleNames.get(0))
                .roles(roleNames)
                .screens(screens)
                .build();
    }

    private ScreenPermissionsDTO mergePermissions(ScreenPermissionsDTO current, ScreenRole screenRole) {
        if (current == null) {
            current = ScreenPermissionsDTO.builder()
                    .canView(false)
                    .canCreate(false)
                    .canEdit(false)
                    .canDelete(false)
                    .canExport(false)
                    .build();
        }

        current.setCanView(Boolean.TRUE.equals(current.getCanView()) || Boolean.TRUE.equals(screenRole.getCanView()));
        current.setCanCreate(Boolean.TRUE.equals(current.getCanCreate()) || Boolean.TRUE.equals(screenRole.getCanCreate()));
        current.setCanEdit(Boolean.TRUE.equals(current.getCanEdit()) || Boolean.TRUE.equals(screenRole.getCanEdit()));
        current.setCanDelete(Boolean.TRUE.equals(current.getCanDelete()) || Boolean.TRUE.equals(screenRole.getCanDelete()));
        current.setCanExport(Boolean.TRUE.equals(current.getCanExport()) || Boolean.TRUE.equals(screenRole.getCanExport()));

        return current;
    }

    @Transactional(readOnly = true)
    public ScreenPermissionsDTO getPermissionsForUserOnScreen(String username, String screenCode) {
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
        

        Screen screen = screenRepository.findByCode(screenCode)
                .orElseThrow(() -> new ResourceNotFoundException("Screen non trouvé: " + screenCode));
        

        ScreenPermissionsDTO permissions = ScreenPermissionsDTO.builder()
                .canView(false)
                .canCreate(false)
                .canEdit(false)
                .canDelete(false)
                .canExport(false)
                .build();

        // Vérifier les permissions pour tous les rôles de l'utilisateur
        for (Role role : user.getRoles()) {
            
            ScreenRole screenRole = screenRoleRepository.findByScreenIdAndRoleId(screen.getId(), role.getId())
                    .orElse(null);
            
            if (screenRole != null) {
                // Appliquer l'union des permissions (si un rôle permet, on permet)
                permissions = mergePermissions(permissions, screenRole);
            }
        }
        
        return permissions;
    }

    @Transactional
    public ScreenDTO createScreen(ScreenDTO screenDTO) {
        Screen screen = Screen.builder()
                .code(screenDTO.getCode())
                .libelle(screenDTO.getLibelle())
                .description(screenDTO.getDescription())
                .route(screenDTO.getRoute())
                .icon(screenDTO.getIcon())
                .ordre(screenDTO.getOrdre())
                .parentId(screenDTO.getParentId())
                .actif(screenDTO.getActif() != null ? screenDTO.getActif() : true)
                .build();

        Screen savedScreen = screenRepository.save(screen);

        auditService.logAction("CREATE", "Screen", savedScreen.getId().toString(),
                "Screen créé: " + savedScreen.getLibelle(), "SUCCESS");

        return mapToDTO(savedScreen);
    }

    @Transactional
    public ScreenDTO updateScreen(Long id, ScreenDTO screenDTO) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen non trouvé avec l'id: " + id));

        screen.setLibelle(screenDTO.getLibelle());
        screen.setDescription(screenDTO.getDescription());
        screen.setRoute(screenDTO.getRoute());
        screen.setIcon(screenDTO.getIcon());
        screen.setOrdre(screenDTO.getOrdre());
        screen.setParentId(screenDTO.getParentId());
        screen.setActif(screenDTO.getActif());

        Screen updatedScreen = screenRepository.save(screen);

        auditService.logAction("UPDATE", "Screen", updatedScreen.getId().toString(),
                "Screen modifié: " + updatedScreen.getLibelle(), "SUCCESS");

        return mapToDTO(updatedScreen);
    }

    @Transactional
    public void deleteScreen(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen non trouvé avec l'id: " + id));

        screenRepository.delete(screen);

        auditService.logAction("DELETE", "Screen", id.toString(),
                "Screen supprimé: " + screen.getLibelle(), "SUCCESS");
    }

    @Transactional(readOnly = true)
    public List<ScreenRoleDTO> getPermissionMatrix() {
        return screenRoleRepository.findAll().stream()
                .map(this::mapScreenRoleToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ScreenRoleDTO> copyRoleProfile(Long sourceRoleId, Long targetRoleId) {
        if (sourceRoleId.equals(targetRoleId)) {
            throw new BusinessException("Les profils source et cible doivent etre differents");
        }

        Role sourceRole = roleRepository.findById(sourceRoleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role source non trouve: " + sourceRoleId));
        Role targetRole = roleRepository.findById(targetRoleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role cible non trouve: " + targetRoleId));
        List<ScreenRole> sourcePermissions = screenRoleRepository.findByRoleId(sourceRoleId);

        screenRoleRepository.deleteAll(screenRoleRepository.findByRoleId(targetRoleId));
        screenRoleRepository.flush();

        List<ScreenRole> copied = sourcePermissions.stream()
                .filter(source -> "ROLE_ADMIN".equals(targetRole.getName().toString())
                        || !"GESTION_PERMISSIONS".equals(source.getScreen().getCode()))
                .map(source -> ScreenRole.builder()
                        .screen(source.getScreen())
                        .role(targetRole)
                        .canView(source.getCanView())
                        .canCreate(source.getCanCreate())
                        .canEdit(source.getCanEdit())
                        .canDelete(source.getCanDelete())
                        .canExport(source.getCanExport())
                        .build())
                .collect(Collectors.toList());

        List<ScreenRoleDTO> result = screenRoleRepository.saveAll(copied).stream()
                .map(this::mapScreenRoleToDTO)
                .collect(Collectors.toList());

        auditService.logAction("COPY_PROFILE", "ScreenRole", targetRoleId.toString(),
                "Profil " + sourceRole.getName() + " copie vers " + targetRole.getName()
                        + " (" + result.size() + " ecrans)", "SUCCESS");
        return result;
    }

    @Transactional
    public ScreenRoleDTO assignRoleToScreen(Long screenId, Long roleId, ScreenPermissionsDTO permissions) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen non trouvé avec l'id: " + screenId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role non trouvé avec l'id: " + roleId));

        ScreenRole screenRole = screenRoleRepository.findByScreenIdAndRoleId(screenId, roleId)
                .orElse(ScreenRole.builder()
                        .screen(screen)
                        .role(role)
                        .build());
        Map<String, Object> oldValues = permissionValues(screenRole);

        screenRole.setCanView(permissions.getCanView() != null ? permissions.getCanView() : true);
        screenRole.setCanCreate(permissions.getCanCreate() != null ? permissions.getCanCreate() : false);
        screenRole.setCanEdit(permissions.getCanEdit() != null ? permissions.getCanEdit() : false);
        screenRole.setCanDelete(permissions.getCanDelete() != null ? permissions.getCanDelete() : false);
        screenRole.setCanExport(permissions.getCanExport() != null ? permissions.getCanExport() : false);

        ScreenRole savedScreenRole = screenRoleRepository.save(screenRole);

        auditService.logUpdate("ScreenRole", savedScreenRole.getId().toString(),
                screen.getCode() + ":" + role.getName(), oldValues, permissionValues(savedScreenRole),
                "Permissions du role " + role.getName() + " modifiees sur " + screen.getLibelle());

        return mapScreenRoleToDTO(savedScreenRole);
    }

    @Transactional
    public void removeRoleFromScreen(Long screenId, Long roleId) {
        screenRoleRepository.deleteByScreenIdAndRoleId(screenId, roleId);

        auditService.logAction("REMOVE", "ScreenRole", screenId + "-" + roleId,
                "Rôle retiré du screen", "SUCCESS");
    }

    @Transactional(readOnly = true)
    public List<ScreenRoleDTO> getScreenRoles(Long screenId) {
        return screenRoleRepository.findByScreenId(screenId).stream()
                .map(this::mapScreenRoleToDTO)
                .collect(Collectors.toList());
    }

    private Map<String, Object> permissionValues(ScreenRole screenRole) {
        return auditService.values(
                "canView", Boolean.TRUE.equals(screenRole.getCanView()),
                "canCreate", Boolean.TRUE.equals(screenRole.getCanCreate()),
                "canEdit", Boolean.TRUE.equals(screenRole.getCanEdit()),
                "canDelete", Boolean.TRUE.equals(screenRole.getCanDelete()),
                "canExport", Boolean.TRUE.equals(screenRole.getCanExport()));
    }
    private ScreenDTO mapToDTO(Screen screen) {
        List<String> roles = screen.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList());

        return ScreenDTO.builder()
                .id(screen.getId())
                .code(screen.getCode())
                .libelle(screen.getLibelle())
                .description(screen.getDescription())
                .route(screen.getRoute())
                .icon(screen.getIcon())
                .ordre(screen.getOrdre())
                .parentId(screen.getParentId())
                .actif(screen.getActif())
                .roles(roles)
                .build();
    }

    private ScreenRoleDTO mapScreenRoleToDTO(ScreenRole screenRole) {
        return ScreenRoleDTO.builder()
                .id(screenRole.getId())
                .screenId(screenRole.getScreen().getId())
                .screenCode(screenRole.getScreen().getCode())
                .screenLibelle(screenRole.getScreen().getLibelle())
                .roleId(screenRole.getRole().getId())
                .roleName(screenRole.getRole().getName().toString())
                .canView(screenRole.getCanView())
                .canCreate(screenRole.getCanCreate())
                .canEdit(screenRole.getCanEdit())
                .canDelete(screenRole.getCanDelete())
                .canExport(screenRole.getCanExport())
                .build();
    }
}
