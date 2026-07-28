package com.smartinventorysystem.modules.notification.repository;

import com.smartinventorysystem.modules.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository  extends JpaRepository<Notification, Integer> {

    List<Notification> findAllByUserIDOrderByCreatedAtDesc(Integer userID);

    List<Notification> findByUserIDAndIsReadFalseOrderByCreatedAtDesc(Integer userID);

    Optional<Notification> findByNotificationIDAndUserID(Integer notificationID, Integer userID);

    long countByUserIDAndIsReadFalse(Integer userID);

    List<Notification> findTop10ByUserIDOrderByCreatedAtDesc(Integer userID);
}