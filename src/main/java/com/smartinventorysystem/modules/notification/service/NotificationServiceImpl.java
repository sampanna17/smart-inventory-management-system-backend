package com.smartinventorysystem.modules.notification.service;

import com.smartinventorysystem.constants.MessageConstants;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

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
    @Transactional
    public void createNotification(
            Integer userId,
            String title,
            String message,
            NotificationType type) {


        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND_WITH_ID + userId
                        )
                );

        Notification notification = new Notification();

        notification.setUserID(user.getUserID());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(
                LocalDateTime.now(clock)
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSend(
                "/topic/notifications/" + user.getUserID(),
                notificationMapper.toResponse(savedNotification)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotificationsForCurrentUser(){

        Integer currentUserId =
                authenticatedUserProvider.getCurrentUserId();

        return notificationMapper.toResponseList(
                notificationRepository
                        .findAllByUserIDOrderByCreatedAtDesc(currentUserId)
        );

    }

    @Override
    @Transactional
    public NotificationResponse markNotificationAsRead(
            Integer notificationId){

        Integer currentUserId =
                authenticatedUserProvider.getCurrentUserId();

        Notification notification =
                notificationRepository
                        .findByNotificationIDAndUserID(
                                notificationId,
                                currentUserId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                )
                        );

        notification.setIsRead(true);

        return notificationMapper.toResponse(
                notificationRepository.save(notification)
        );

    }

    @Override
    @Transactional
    public void markAllNotificationsAsRead(){

        Integer currentUserId =
                authenticatedUserProvider.getCurrentUserId();

        List<Notification> notifications =
                notificationRepository
                        .findByUserIDAndIsReadFalseOrderByCreatedAtDesc(
                                currentUserId
                        );

        if(notifications.isEmpty()){
            return;
        }

        notifications.forEach(
                notification ->
                        notification.setIsRead(true)
        );

        notificationRepository.saveAll(notifications);

    }

    @Override
    @Transactional
    public void deleteNotification(
            Integer notificationId){

        Integer currentUserId =
                authenticatedUserProvider.getCurrentUserId();

        Notification notification =
                notificationRepository
                        .findByNotificationIDAndUserID(
                                notificationId,
                                currentUserId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                )
                        );

        notificationRepository.delete(notification);

    }

    @Override
    @Transactional
    public void deleteAllNotifications(){

        Integer currentUserId =
                authenticatedUserProvider.getCurrentUserId();

        notificationRepository.deleteAllByUserID(currentUserId);

    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(){

        Integer currentUserId =
                authenticatedUserProvider.getCurrentUserId();

        return notificationRepository
                .countByUserIDAndIsReadFalse(
                        currentUserId
                );

    }
}