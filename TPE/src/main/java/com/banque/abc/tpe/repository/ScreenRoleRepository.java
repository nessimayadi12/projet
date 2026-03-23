package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.ScreenRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenRoleRepository extends JpaRepository<ScreenRole, Long> {
    
    List<ScreenRole> findByScreenId(Long screenId);
    
    List<ScreenRole> findByRoleId(Long roleId);
    
    Optional<ScreenRole> findByScreenIdAndRoleId(Long screenId, Long roleId);
    
    @Query("SELECT sr FROM ScreenRole sr WHERE sr.screen.code = :screenCode AND sr.role.name = :roleName")
    Optional<ScreenRole> findByScreenCodeAndRoleName(@Param("screenCode") String screenCode, @Param("roleName") String roleName);
    
    @Query("SELECT sr FROM ScreenRole sr WHERE sr.screen.route = :route AND sr.role.name = :roleName")
    Optional<ScreenRole> findByScreenRouteAndRoleName(@Param("route") String route, @Param("roleName") String roleName);
    
    void deleteByScreenIdAndRoleId(Long screenId, Long roleId);
}
