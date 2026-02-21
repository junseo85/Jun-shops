package com.dailyproject.Junshops.service.user;

import com.dailyproject.Junshops.dto.UserDto;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.request.CreateUserRequest;
import com.dailyproject.Junshops.request.UserUpdateRequest;

import java.util.List;
import java.util.Set;

public interface IUserService {

    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);

    UserDto convertUserToDto(User user);

    User getAuthenticatedUser();
    User getAuthenticatedUserWithCart();

    //Get all users(admin only)
    List<User> getAllUsers();

    //update user role
    void updateUserRoles(Long userId, Set<String> roleNames);
}
