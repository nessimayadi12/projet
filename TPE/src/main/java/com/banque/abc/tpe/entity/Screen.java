package com.banque.abc.tpe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, unique = true)
    private String route;

    @Column(name = "icon")
    private String icon;

    @Column(name = "ordre")
    private Integer ordre;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private Boolean actif = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "screen_roles",
        joinColumns = @JoinColumn(name = "screen_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
