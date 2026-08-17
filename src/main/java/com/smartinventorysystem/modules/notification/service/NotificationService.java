package com.smartinventorysystem.modules.notification.service;

import com.smartinventorysystem.enums.NotificationType;
import com.smartinventorysystem.modules.notification.dto.response.NotificationResponse;

import java.util.List;

import com.smartinventorysystem.modules.notification.dto.request.CreateNotificationRequest;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

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

    void broadcastNotification(CreateNotificationRequest request);

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