package com.smartinventorysystem.modules.user.service;

import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.enums.Role;
import com.smartinventorysystem.enums.Status;
import com.smartinventorysystem.exceptions.BadRequestException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.user.dto.response.UserResponse;
import com.smartinventorysystem.modules.user.mapper.UserMapper;
import com.smartinventorysystem.common.email.EmailService;
import com.smartinventorysystem.modules.user.dto.request.CreateStaffRequest;
import com.smartinventorysystem.modules.user.dto.response.CreateStaffResponse;
import com.smartinventorysystem.modules.user.repository.UserRepository;
import com.smartinventorysystem.modules.user.dto.request.UpdateProfileRequest;
import com.smartinventorysystem.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.user.dto.request.UserFilterRequest;
import com.smartinventorysystem.modules.user.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.smartinventorysystem.enums.NotificationType;
import com.smartinventorysystem.modules.notification.service.NotificationService;
import com.smartinventorysystem.utils.AuthenticatedUserProvider;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final Clock clock;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final NotificationService notificationService;

    @Override
    public CreateStaffResponse createStaff(CreateStaffRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // generate activation token
        String token = UUID.randomUUID().toString();

        User staff = new User();
        staff.setFullName(request.getFullName());
        staff.setEmail(request.getEmail());
        staff.setPasswordHash(null);
        staff.setRole(Role.STAFF);
        staff.setStatus(Status.INACTIVE);
        staff.setActivationToken(token);
        staff.setTokenExpiry(LocalDateTime.now(clock).plusHours(24));
        staff.setCreatedAt(LocalDateTime.now(clock));

        User savedStaff = userRepository.save(staff);

        emailService.sendStaffAccountCreatedEmail(
                savedStaff.getEmail(),
                savedStaff.getFullName(),
                token
        );

        Integer currentAdminId = authenticatedUserProvider.getCurrentUserId();
        notificationService.notifyUserAndAdmins(
                currentAdminId,
                "Staff Account Created",
                "Staff account for " + savedStaff.getFullName() + " (" + savedStaff.getEmail() + ") was successfully created.",
                NotificationType.STAFF_ACCOUNT_CREATED
        );

        return userMapper.toCreateStaffResponse(savedStaff);
    }

    @Override
    public UserResponse updateProfile(Integer userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        // update only allowed fields (
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {

            // prevent duplicate email
            boolean emailExists = userRepository.existsByEmail(request.getEmail());
            if (emailExists) {
                throw new BadRequestException("Email already in use");
            }

            user.setEmail(request.getEmail());
        }

        user.setUpdatedAt(LocalDateTime.now(clock));

        User updated = userRepository.save(user);

        return userMapper.toResponse(updated);
    }

    @Override
    public void deleteAdmin(Integer adminId) {

        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only admin accounts can be deleted.");
        }

        userRepository.delete(user);
    }

    @Override
    public void deleteStaff(Integer staffId) {

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STAFF_NOT_FOUND));

        if (staff.getRole() != Role.STAFF) {
            throw new BadRequestException("Only staff accounts can be deleted.");
        }

        userRepository.delete(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(UserFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<User> specification = UserSpecification.withFilters(request);

        Page<User> userPage = userRepository.findAll(specification, pageable);
        return PageResponse.of(userPage, userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    private Pageable createPageable(UserFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "createdAt";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "desc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "createdAt";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "name", "fullname" -> "fullName";
            case "email" -> "email";
            case "role" -> "role";
            case "status" -> "status";
            case "date", "createdat" -> "createdAt";
            case "updatedat" -> "updatedAt";
            case "id", "userid" -> "userID";
            default -> "createdAt";
        };
    }

    @Override
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        return userMapper.toResponse(user);
    }

    @Override
    public void deactivateStaff(Integer staffId) {

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STAFF_NOT_FOUND));

        if (staff.getRole() != Role.STAFF) {
            throw new BadRequestException("Only staff accounts can be deactivated.");
        }

        if (staff.getStatus() == Status.INACTIVE) {
            throw new BadRequestException("Staff account is already inactive.");
        }

        staff.setStatus(Status.INACTIVE);

        userRepository.save(staff);
    }

    @Override
    public void activateStaff(Integer staffId) {

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STAFF_NOT_FOUND));

        if (staff.getRole() != Role.STAFF) {
            throw new BadRequestException("Only staff accounts can be activated.");
        }

        if (staff.getStatus() == Status.ACTIVE) {
            throw new BadRequestException("Staff account is already active.");
        }

        staff.setStatus(Status.ACTIVE);

        userRepository.save(staff);
    }

    @Override
    public String getUserFullName(Integer userId) {

        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .map(User::getFullName)
                .orElse(null);
    }

    @Override
    public Map<Integer, String> getUserFullNames(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, String> map = new HashMap<>();
        userRepository.findAllById(userIds).forEach(user -> {
            if (user.getUserID() != null) {
                String name = user.getFullName() != null && !user.getFullName().isBlank()
                        ? user.getFullName()
                        : user.getEmail();
                map.put(user.getUserID(), name != null ? name : "User #" + user.getUserID());
            }
        });
        return map;
    }

}
