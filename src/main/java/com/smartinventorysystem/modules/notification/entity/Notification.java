package com.smartinventorysystem.modules.notification.entity;

import com.smartinventorysystem.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationID;

    private Integer userID;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Boolean isRead = false;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private LocalDateTime createdAt;
}
