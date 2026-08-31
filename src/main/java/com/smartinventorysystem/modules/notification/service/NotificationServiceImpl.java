package com.smartinventorysystem.modules.notification.service;

import com.smartinventorysystem.enums.NotificationType;
import com.smartinventorysystem.modules.notification.dto.response.NotificationResponse;
import com.smartinventorysystem.modules.notification.entity.Notification;
import com.smartinventorysystem.modules.notification.mapper.NotificationMapper;
import com.smartinventorysystem.modules.notification.repository.NotificationRepository;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.user.entity.User;
import com.smartinventorysystem.modules.user.repository.UserRepository;
import com.smartinventorysystem.utils.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.smartinventorysystem.enums.Role;
import java.util.HashSet;
import java.util.Set;

import com.smartinventorysystem.modules.notification.dto.request.CreateNotificationRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Clock clock;

    @Override
    @Transactional("simsTransactionManager")
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Integer targetUserId = request.getUserId() != null
                ? request.getUserId()
                : authenticatedUserProvider.getCurrentUserId();

        Notification notification = new Notification();
        notification.setUserID(targetUserId);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now(clock));

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification successfully inserted in DB with ID: {} for user: {}", savedNotification.getNotificationID(), targetUserId);
        NotificationResponse response = notificationMapper.toResponse(savedNotification);

        try {
            simpMessagingTemplate.convertAndSend(
                    "/topic/notifications/" + targetUserId,
                    response);
        } catch (Exception ex) {
            log.warn("WebSocket push failed for user {}: {}", targetUserId, ex.getMessage());
        }

        return response;
    }

    @Async
    @Override
    public void broadcastNotification(CreateNotificationRequest request) {
        try {
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                createNotification(user.getUserID(), request.getTitle(), request.getMessage(), request.getType());
            }
        } catch (Exception ex) {
            log.error("Failed to broadcast notification: {}", ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional("simsTransactionManager")
    public void createNotification(
            Integer userId,
            String title,
            String message,
            NotificationType type) {

        if (userId == null) {
            return;
        }

        try {
            Notification notification = new Notification();
            notification.setUserID(userId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now(clock));

            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification successfully inserted in DB with ID: {} for user: {}", savedNotification.getNotificationID(), userId);

            try {
                simpMessagingTemplate.convertAndSend(
                        "/topic/notifications/" + userId,
                        notificationMapper.toResponse(savedNotification));
            } catch (Exception ex) {
                log.warn("WebSocket push failed for user {}: {}", userId, ex.getMessage());
            }
        } catch (Exception ex) {
            log.error("Failed to save notification for user {}: {}", userId, ex.getMessage(), ex);
        }
    }

    @Async
    @Override
    public void notifyUserAndAdmins(
            Integer userId,
            String title,
            String message,
            NotificationType type) {
        try {
            Set<Integer> targetUserIds = new HashSet<>();
            if (userId != null) {
                targetUserIds.add(userId);
            }
            try {
                List<User> admins = userRepository.findByRole(Role.ADMIN);
                if (admins != null) {
                    admins.forEach(admin -> targetUserIds.add(admin.getUserID()));
                }
            } catch (Exception e) {
                log.warn("Could not query admin list for notifications: {}", e.getMessage());
            }
            for (Integer targetId : targetUserIds) {
                createNotification(targetId, title, message, type);
            }
        } catch (Exception ex) {
            log.error("Error in notifyUserAndAdmins: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @Override
    public void notifyAdmins(
            String title,
            String message,
            NotificationType type) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            if (admins != null) {
                for (User admin : admins) {
                    createNotification(admin.getUserID(), title, message, type);
                }
            }
        } catch (Exception ex) {
            log.error("Error in notifyAdmins: {}", ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public List<NotificationResponse> getAllNotificationsForCurrentUser() {

        Integer currentUserId = authenticatedUserProvider.getCurrentUserId();

        return notificationMapper.toResponseList(
                notificationRepository
                        .findAllByUserIDOrderByCreatedAtDesc(currentUserId));

    }

    @Override
    @Transactional("simsTransactionManager")
    public NotificationResponse markNotificationAsRead(
            Integer notificationId) {

        Integer currentUserId = authenticatedUserProvider.getCurrentUserId();

        Notification notification = notificationRepository
                .findByNotificationIDAndUserID(
                        notificationId,
                        currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with ID: "
                                + notificationId));

        notification.setIsRead(true);

        return notificationMapper.toResponse(
                notificationRepository.save(notification));

    }

    @Override
    @Transactional("simsTransactionManager")
    public void markAllNotificationsAsRead() {

        Integer currentUserId = authenticatedUserProvider.getCurrentUserId();

        List<Notification> notifications = notificationRepository
                .findByUserIDAndIsReadFalseOrderByCreatedAtDesc(
                        currentUserId);

        if (notifications.isEmpty()) {
            return;
        }

        notifications.forEach(
                notification -> notification.setIsRead(true));

        notificationRepository.saveAll(notifications);

    }

    @Override
    @Transactional("simsTransactionManager")
    public void deleteNotification(
            Integer notificationId) {

        Integer currentUserId = authenticatedUserProvider.getCurrentUserId();

        Notification notification = notificationRepository
                .findByNotificationIDAndUserID(
                        notificationId,
                        currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with ID: "
                                + notificationId));

        notificationRepository.delete(notification);

    }

    @Override
    @Transactional("simsTransactionManager")
    public void deleteAllNotifications() {

        Integer currentUserId = authenticatedUserProvider.getCurrentUserId();

        notificationRepository.deleteAllByUserID(currentUserId);

    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public long getUnreadNotificationCount() {

        Integer currentUserId = authenticatedUserProvider.getCurrentUserId();

        return notificationRepository
                .countByUserIDAndIsReadFalse(
                        currentUserId);

    }
}