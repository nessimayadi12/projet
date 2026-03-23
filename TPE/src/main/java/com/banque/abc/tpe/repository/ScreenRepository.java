package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    
    Optional<Screen> findByCode(String code);
    
    Optional<Screen> findByRoute(String route);
    
    List<Screen> findByActifTrue();
    
    List<Screen> findByParentIdIsNull();
    
    List<Screen> findByParentId(Long parentId);
    
    @Query("SELECT s FROM Screen s JOIN s.roles r WHERE r.id = :roleId AND s.actif = true")
    List<Screen> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT DISTINCT s FROM Screen s JOIN s.roles r WHERE r.name = :roleName AND s.actif = true ORDER BY s.ordre")
    List<Screen> findByRoleName(@Param("roleName") String roleName);
}
