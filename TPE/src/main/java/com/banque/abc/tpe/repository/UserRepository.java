package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.RoleType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    @Query("""
            select distinct u from User u
            join u.roles r
            where u.actif = true and r.name in :roles
            """)
    List<User> findActiveUsersByRoles(@Param("roles") Collection<RoleType> roles);

    @Query("""
            select distinct u from User u
            join u.roles r
            where u.actif = true
              and r.name = :role
              and lower(u.codeAgence) = lower(:codeAgence)
            """)
    List<User> findActiveUsersByRoleAndCodeAgence(@Param("role") RoleType role,
                                                  @Param("codeAgence") String codeAgence);
}
