package com.smartinventorysystem.modules.user.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.user.dto.request.CreateStaffRequest;
import com.smartinventorysystem.modules.user.dto.request.UpdateProfileRequest;
import com.smartinventorysystem.modules.user.dto.request.UserFilterRequest;
import com.smartinventorysystem.modules.user.dto.response.CreateStaffResponse;
import com.smartinventorysystem.modules.user.dto.response.UserResponse;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponse updateProfile(Integer userId, UpdateProfileRequest request);
    void deleteAdmin(Integer adminId);
    void deleteStaff(Integer staffId);
    CreateStaffResponse createStaff(CreateStaffRequest request);
    PageResponse<UserResponse> getUsers(UserFilterRequest filterRequest);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Integer userId);
    void deactivateStaff(Integer staffId);
    void activateStaff(Integer userId);
    String getUserFullName(Integer userId);
    Map<Integer, String> getUserFullNames(List<Integer> userIds);
}
