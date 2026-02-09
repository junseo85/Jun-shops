package com.dailyproject.Junshops.service.user;

import com.dailyproject.Junshops.dto.UserDto;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.request.CreateUserRequest;
import com.dailyproject.Junshops.request.UserUpdateRequest;

public interface IUserService {

    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);

    UserDto convertUserToDto(User user);

    User getAuthenticatedUser();
}
