package com.banque.abc.tpe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenDTO {
    private Long id;
    private String code;
    private String libelle;
    private String description;
    private String route;
    private String icon;
    private Integer ordre;
    private Long parentId;
    private Boolean actif;
    private List<String> roles;
    private ScreenPermissionsDTO permissions;
}
