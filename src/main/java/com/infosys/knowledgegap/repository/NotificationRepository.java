package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
    Optional<Notification> findByUserIdAndDedupeKey(Long userId, String dedupeKey);
}
