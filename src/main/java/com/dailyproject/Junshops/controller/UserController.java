package com.dailyproject.Junshops.controller;

import com.dailyproject.Junshops.dto.UserDto;
import com.dailyproject.Junshops.exceptions.AlreadyExistsException;
import com.dailyproject.Junshops.exceptions.ResourceNotFoundException;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.request.CreateUserRequest;
import com.dailyproject.Junshops.request.UserUpdateRequest;
import com.dailyproject.Junshops.response.ApiResponse;
import com.dailyproject.Junshops.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * REST controller for user CRUD operations.
 *
 * <p>Delegates business logic (validation, persistence, mapping) to {@link IUserService}.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    /**
     * Retrieves a user by id.
     *
     * @param userId user identifier
     * @return user details as {@link UserDto}
     */
    @GetMapping("/{userId}/user")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId){
        // Responds with user or not found message
        try {
            User user = userService.getUserById(userId);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Success!", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    /**
     * Creates a new user.
     *
     * <p>Expected behavior: email uniqueness enforced; password encoded in service layer.</p>
     *
     * @param request request payload for creating a user
     * @return created user as {@link UserDto}
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest request){
        // Creates user; responds with success or conflict
        try {
            User user = userService.createUser(request);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Create User Success!", userDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    /**
     * Updates user profile fields (e.g., first and last name).
     *
     * @param request updated user fields
     * @param userId user identifier
     * @return updated user as {@link UserDto}
     */
    @PutMapping("/{userId}/update")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserUpdateRequest request, @PathVariable Long userId) {
        // Updates user; returns result or handles not found
        try {
            User user = userService.updateUser(request, userId);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Update User Success!", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    /**
     * Deletes a user by id.
     *
     * @param userId user identifier
     * @return success message on deletion
     */
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId){
        // Deletes user; returns success or handles not found
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse("Delete User Success!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
