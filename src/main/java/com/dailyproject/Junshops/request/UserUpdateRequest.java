package com.dailyproject.Junshops.request;

import lombok.Data;

/**
 * Request payload for updating a user's basic profile fields.
 *
 * <p>Used by {@code UserController#updateUser(UserUpdateRequest, Long)}.</p>
 *
 * <p>Note: Validation annotations (e.g. {@code @NotBlank}) can be added later if required.</p>
 */
@Data
public class UserUpdateRequest {
    /** User's updated first name (optional depending on validation rules). */
    private String firstName;

    /** User's updated last name (optional depending on validation rules). */
    private String lastName;
}