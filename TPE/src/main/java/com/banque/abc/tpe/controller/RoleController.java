package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.RoleDTO;
import com.banque.abc.tpe.entity.Role;
import com.banque.abc.tpe.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(role -> {
                    String label = getRoleLabel(role.getName().toString());
                    return RoleDTO.builder()
                            .id(role.getId())
                            .name(role.getName().toString())
                            .label(label)
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleDTOs);
    }

    private String getRoleLabel(String roleName) {
        switch(roleName) {
            case "ROLE_ADMIN": return "Administrateur";
            case "ROLE_MONETIQUE": return "Monétique";
            case "ROLE_AGENCE": return "Agence";
            case "ROLE_INPUTER": return "Saisisseur";
            case "ROLE_AUTHORIZER": return "Valideur";
            case "ROLE_TECHNICIEN": return "Technicien";
            case "ROLE_COMMERCANT": return "Commerçant";
            case "ROLE_LOGISTIQUE": return "Logistique";
            default: return roleName;
        }
    }
}
