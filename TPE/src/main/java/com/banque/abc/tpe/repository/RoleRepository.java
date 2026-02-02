package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.Role;
import com.banque.abc.tpe.entity.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(RoleType name);
}
