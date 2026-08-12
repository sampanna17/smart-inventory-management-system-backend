package com.smartinventorysystem.modules.user.controller;

import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.user.dto.request.UserFilterRequest;
import com.smartinventorysystem.modules.user.dto.request.CreateStaffRequest;
import com.smartinventorysystem.modules.user.dto.request.UpdateProfileRequest;
import com.smartinventorysystem.modules.user.dto.response.CreateStaffResponse;
import com.smartinventorysystem.modules.user.dto.response.UserResponse;
import com.smartinventorysystem.modules.user.entity.User;
import com.smartinventorysystem.modules.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(ApiRoutes.Users.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Clock clock;

    @PatchMapping(ApiRoutes.Users.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse response = userService.updateProfile(user.getUserID(), request);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Profile updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Users.DELETE_ADMIN)
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Integer adminId) {

        userService.deleteAdmin(adminId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Admin deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Users.DELETE_STAFF)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Integer staffId) {

        userService.deleteStaff(staffId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Staff deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Users.CREATE_STAFF)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreateStaffResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request) {

        CreateStaffResponse response = userService.createStaff(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CreateStaffResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Staff created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @Valid @ModelAttribute UserFilterRequest filterRequest) {

        PageResponse<UserResponse> response = userService.getUsers(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<UserResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Users fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("All users fetched successfully")
                        .data(users)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Users.GET_BY_ID)
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Integer userId) {

        UserResponse user = userService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("User fetched successfully")
                        .data(user)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PatchMapping(ApiRoutes.Users.DEACTIVATE_STAFF)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateStaff(
            @PathVariable Integer staffId) {

        userService.deactivateStaff(staffId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Staff account deactivated successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PatchMapping(ApiRoutes.Users.ACTIVATE_STAFF)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateStaff(
            @PathVariable Integer staffId) {

        userService.activateStaff(staffId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Staff account activated successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}

