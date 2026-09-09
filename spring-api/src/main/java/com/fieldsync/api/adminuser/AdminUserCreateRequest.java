package com.fieldsync.api.adminuser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record AdminUserCreateRequest(

    @NotBlank(
        message = "Full name is required"
    )
    @Size(
        min = 2,
        max = 150,
        message = "Full name must be between 2 and 150 characters"
    )
    @Pattern(
        regexp =
            "^[\\p{L}\\p{M}][\\p{L}\\p{M} .'-]*$",
        message =
            "Full name contains unsupported characters"
    )
    String fullName,


    @NotBlank(
        message = "Username is required"
    )
    @Size(
        min = 3,
        max = 100,
        message =
            "Username must be between 3 and 100 characters"
    )
    @Pattern(
        regexp =
            "^[A-Za-z0-9._-]+$",
        message =
            "Username may contain only letters, numbers, dots, underscores and hyphens"
    )
    String username,


    @NotBlank(
        message = "Email is required"
    )
    @Email(
        message = "Enter a valid email address"
    )
    @Size(
        max = 150,
        message =
            "Email must not exceed 150 characters"
    )
    String email,


    @NotNull(
        message = "Web access selection is required"
    )
    Boolean accessWeb,


    @NotNull(
        message = "Mobile access selection is required"
    )
    Boolean accessMobile

) {
}