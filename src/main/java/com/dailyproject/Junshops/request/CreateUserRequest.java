package com.dailyproject.Junshops.request;

import lombok.Data;


/**
 * Request payload for registering/creating a new user.
 *
 * <p>Used by {@code UserController#createUser(CreateUserRequest)}.</p>
 *
 * <p>Security note: the password is expected to be encoded in the service layer
 * before persisting the user.</p>
 */
@Data
public class CreateUserRequest {
    /** User's first name. */
    private String firstName;

    /** User's last name. */
    private String lastName;

    /** User's email; typically used as the login/username. */
    private String email;

    /** Raw password from client; must be encoded before storage. */
    private String password;
}