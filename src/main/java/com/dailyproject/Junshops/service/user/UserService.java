package com.dailyproject.Junshops.service.user;

import com.dailyproject.Junshops.data.RoleRepository;
import com.dailyproject.Junshops.dto.UserDto;
import com.dailyproject.Junshops.exceptions.AlreadyExistsException;
import com.dailyproject.Junshops.exceptions.ResourceNotFoundException;
import com.dailyproject.Junshops.model.Role;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.repository.UserRepository;
import com.dailyproject.Junshops.request.CreateUserRequest;
import com.dailyproject.Junshops.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    //add RoleRepository injection
    private final RoleRepository roleRepository;

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        // Creates user if email is unique; throws exception otherwise
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    return userRepository.save(user);
                }).orElseThrow(() -> new AlreadyExistsException("Oops!" + request.getEmail() + " is already registered!"));
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {

        // Updates user if found; throws exception if missing
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresentOrElse(userRepository::delete, () -> {
            throw new ResourceNotFoundException("User not found!");
        });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Override
    @Transactional(readOnly = true)
    public User getAuthenticatedUserWithCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailWithCart(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }
    // Add this method to UserService.java

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Update user roles
     * <p>
     * PROCESS:
     * 1. Find user
     * 2. Find roles by names
     * 3. Replace user's roles with new set
     * 4. Save user
     * <p>
     * WHY separate method?
     * - Role management is sensitive operation
     * - Different from updating name/email
     * - Admin-only functionality
     */
    @Override
    @Transactional
    public void updateUserRoles(Long userId, Set<String> roleNames) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get roles from a database
        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        // Update user roles
        user.setRoles(roles);
        userRepository.save(user);
    }
}
