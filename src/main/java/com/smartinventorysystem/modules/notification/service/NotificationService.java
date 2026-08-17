package com.smartinventorysystem.modules.notification.service;

import com.smartinventorysystem.enums.NotificationType;
import com.smartinventorysystem.modules.notification.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void createNotification(
            Integer userId,
            String title,
            String message,
            NotificationType type
    );

    void notifyUserAndAdmins(
            Integer userId,
            String title,
            String message,
            NotificationType type
    );

    void notifyAdmins(
            String title,
            String message,
            NotificationType type
    );

    List<NotificationResponse> getAllNotificationsForCurrentUser();

    NotificationResponse markNotificationAsRead(
            Integer notificationId
    );

    void markAllNotificationsAsRead();

    void deleteNotification(
            Integer notificationId
    );

    void deleteAllNotifications();

    long getUnreadNotificationCount();

}