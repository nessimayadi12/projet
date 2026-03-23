package com.banque.abc.tpe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "screen_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "can_view")
    private Boolean canView = true;

    @Column(name = "can_create")
    private Boolean canCreate = false;

    @Column(name = "can_edit")
    private Boolean canEdit = false;

    @Column(name = "can_delete")
    private Boolean canDelete = false;

    @Column(name = "can_export")
    private Boolean canExport = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
