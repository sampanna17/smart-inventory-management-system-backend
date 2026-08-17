package com.smartinventorysystem.modules.notification.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.modules.notification.dto.response.NotificationResponse;
import com.smartinventorysystem.modules.notification.service.NotificationService;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiRoutes.Notifications.BASE)
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;
	private final Clock clock;

	@GetMapping(ApiRoutes.Notifications.GET_ALL)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotificationsForCurrentUser() {

		List<NotificationResponse> response = notificationService.getAllNotificationsForCurrentUser();

		return ResponseEntity.ok(
				ApiResponse.<List<NotificationResponse>>builder()
						.status(HttpStatus.OK.value())
						.success(true)
						.message("Notifications fetched successfully")
						.data(response)
						.timestamp(LocalDateTime.now(clock))
						.build()
		);
	}

	@PatchMapping(ApiRoutes.Notifications.MARK_AS_READ)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<NotificationResponse>> markNotificationAsRead(
			@PathVariable @Positive Integer notificationId) {

		NotificationResponse response = notificationService.markNotificationAsRead(notificationId);

		return ResponseEntity.ok(
				ApiResponse.<NotificationResponse>builder()
						.status(HttpStatus.OK.value())
						.success(true)
						.message("Notification marked as read successfully")
						.data(response)
						.timestamp(LocalDateTime.now(clock))
						.build()
		);
	}

	@PatchMapping(ApiRoutes.Notifications.MARK_ALL_AS_READ)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead() {

		notificationService.markAllNotificationsAsRead();

		return ResponseEntity.ok(
				ApiResponse.<Void>builder()
						.status(HttpStatus.OK.value())
						.success(true)
						.message("All notifications marked as read successfully")
						.timestamp(LocalDateTime.now(clock))
						.build()
		);
	}

	@DeleteMapping(ApiRoutes.Notifications.DELETE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> deleteNotification(
			@PathVariable @Positive Integer notificationId) {

		notificationService.deleteNotification(notificationId);

		return ResponseEntity.ok(
				ApiResponse.<Void>builder()
						.status(HttpStatus.OK.value())
						.success(true)
						.message("Notification deleted successfully")
						.timestamp(LocalDateTime.now(clock))
						.build()
		);
	}

	@DeleteMapping(ApiRoutes.Notifications.DELETE_ALL)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> deleteAllNotifications() {

		notificationService.deleteAllNotifications();

		return ResponseEntity.ok(
				ApiResponse.<Void>builder()
						.status(HttpStatus.OK.value())
						.success(true)
						.message("All notifications deleted successfully")
						.timestamp(LocalDateTime.now(clock))
						.build()
		);
	}

	@GetMapping(ApiRoutes.Notifications.UNREAD_COUNT)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount() {

		long count = notificationService.getUnreadNotificationCount();

		return ResponseEntity.ok(
				ApiResponse.<Long>builder()
						.status(HttpStatus.OK.value())
						.success(true)
						.message("Unread notification count fetched successfully")
						.data(count)
						.timestamp(LocalDateTime.now(clock))
						.build()
		);
	}
}
