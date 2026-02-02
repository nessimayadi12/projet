package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUsername(String username);
    
    List<AuditLog> findByAction(String action);
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.dateAction BETWEEN :startDate AND :endDate ORDER BY a.dateAction DESC")
    List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username ORDER BY a.dateAction DESC")
    List<AuditLog> findByUsernameOrderByDateDesc(String username);
}
