package com.banque.abc.tpe.repository;

import com.banque.abc.tpe.entity.BusinessNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessNotificationRepository extends JpaRepository<BusinessNotification, Long> {

    List<BusinessNotification> findTop50ByRecipientIdOrderByCreatedDateDesc(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    Optional<BusinessNotification> findByIdAndRecipientId(Long id, Long recipientId);

    @Modifying
    @Query("""
            update BusinessNotification n
            set n.read = true, n.readAt = :readAt
            where n.recipient.id = :recipientId and n.read = false
            """)
    int markAllAsRead(@Param("recipientId") Long recipientId,
                      @Param("readAt") LocalDateTime readAt);
}
