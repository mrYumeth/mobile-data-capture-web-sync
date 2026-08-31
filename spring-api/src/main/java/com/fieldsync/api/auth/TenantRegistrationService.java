package com.fieldsync.api.auth;

import com.fieldsync.api.domain.entity.TenantEntity;
import com.fieldsync.api.domain.entity.UserEntity;

import com.fieldsync.api.domain.repository.TenantRepository;
import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.keycloak.KeycloakAdminClient;
import com.fieldsync.api.keycloak.KeycloakUserConflictException;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

import java.util.Base64;


@Service
public class TenantRegistrationService {

    private static final SecureRandom
        SECURE_RANDOM =
            new SecureRandom();


    private final TenantRepository
        tenantRepository;

    private final UserRepository
        userRepository;

    private final KeycloakAdminClient
        keycloakAdminClient;


    public TenantRegistrationService(

            TenantRepository tenantRepository,

            UserRepository userRepository,

            KeycloakAdminClient keycloakAdminClient
    ) {

        this.tenantRepository =
            tenantRepository;

        this.userRepository =
            userRepository;

        this.keycloakAdminClient =
            keycloakAdminClient;
    }


    @Transactional
    public TenantRegistrationResponse register(
            TenantRegistrationRequest request
    ) {

        String keycloakUserId =
            null;


        try {

            validate(
                request
            );


            String tenantName =
                request
                    .tenantName()
                    .trim();


            String fullName =
                request
                    .fullName()
                    .trim();


            String username =
                request
                    .username()
                    .trim()
                    .toLowerCase();


            String email =
                request
                    .email()
                    .trim()
                    .toLowerCase();


            String slug =
                createTenantSlug(

                    request.tenantSlug() == null ||
                    request.tenantSlug().isBlank()

                        ? tenantName

                        : request.tenantSlug()
                );


            if (slug.isBlank()) {

                throw new TenantRegistrationApiException(
                    HttpStatus.BAD_REQUEST,
                    "A valid company slug is required"
                );
            }


            if (
                tenantRepository
                    .findBySlug(
                        slug
                    )
                    .isPresent()
            ) {

                throw new TenantRegistrationApiException(
                    HttpStatus.CONFLICT,
                    "Company slug is already registered"
                );
            }


            if (
                userRepository
                    .findByUsernameIgnoreCase(
                        username
                    )
                    .isPresent()
                ||
                userRepository
                    .findByEmailIgnoreCase(
                        email
                    )
                    .isPresent()
            ) {

                throw new TenantRegistrationApiException(
                    HttpStatus.CONFLICT,
                    "Admin username or email is already registered"
                );
            }


            TenantEntity tenant =
                tenantRepository
                    .saveAndFlush(

                        TenantEntity.create(
                            tenantName,
                            slug
                        )
                    );


            keycloakUserId =
                keycloakAdminClient
                    .createUser(

                        username,
                        email,
                        fullName,

                        true,
                        true
                    );


            String temporaryPassword =
                generateTemporaryPassword();


            keycloakAdminClient
                .setTemporaryPassword(

                    keycloakUserId,

                    temporaryPassword
                );


            UserEntity admin =
                userRepository
                    .saveAndFlush(

                        UserEntity
                            .createKeycloakAdmin(

                                tenant,

                                username,
                                email,
                                fullName,

                                keycloakUserId
                            )
                    );


            return new TenantRegistrationResponse(

                "Company registered successfully. Temporary Keycloak password generated for the admin.",

                TenantRegistrationTenantResponse
                    .from(
                        tenant
                    ),

                TenantRegistrationUserResponse
                    .from(
                        admin
                    ),

                keycloakUserId,

                temporaryPassword,

                false
            );

        }
        catch (
            TenantRegistrationApiException exception
        ) {

            cleanupKeycloakUser(
                keycloakUserId
            );

            throw exception;

        }
        catch (
            KeycloakUserConflictException exception
        ) {

            cleanupKeycloakUser(
                keycloakUserId
            );

            throw new TenantRegistrationApiException(
                HttpStatus.CONFLICT,
                exception.getMessage()
            );

        }
        catch (
            RuntimeException exception
        ) {

            cleanupKeycloakUser(
                keycloakUserId
            );


            throw new TenantRegistrationApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Company registration failed"
            );
        }
    }


    private void validate(
            TenantRegistrationRequest request
    ) {

        if (
            request == null ||
            request.tenantName() == null ||
            request.tenantName().isBlank()
        ) {

            throw new TenantRegistrationApiException(
                HttpStatus.BAD_REQUEST,
                "Company name is required"
            );
        }


        if (
            request.fullName() == null ||
            request.fullName().isBlank()
        ) {

            throw new TenantRegistrationApiException(
                HttpStatus.BAD_REQUEST,
                "Admin full name is required"
            );
        }


        if (
            request.username() == null ||
            request.username().isBlank()
        ) {

            throw new TenantRegistrationApiException(
                HttpStatus.BAD_REQUEST,
                "Admin username is required"
            );
        }


        if (
            request.email() == null ||
            request.email().isBlank()
        ) {

            throw new TenantRegistrationApiException(
                HttpStatus.BAD_REQUEST,
                "Admin email is required"
            );
        }
    }


    private String createTenantSlug(
            String value
    ) {

        String slug =
            value
                .trim()
                .toLowerCase()
                .replaceAll(
                    "[^a-z0-9]+",
                    "-"
                )
                .replaceAll(
                    "^-+|-+$",
                    ""
                );


        if (
            slug.length() > 100
        ) {

            slug =
                slug.substring(
                    0,
                    100
                );
        }


        return slug;
    }


    private String generateTemporaryPassword() {

        byte[] randomBytes =
            new byte[9];


        SECURE_RANDOM.nextBytes(
            randomBytes
        );


        return "Fs-"
            +
            Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    randomBytes
                )
            +
            "!9";
    }


    private void cleanupKeycloakUser(
            String keycloakUserId
    ) {

        if (
            keycloakUserId == null ||
            keycloakUserId.isBlank()
        ) {
            return;
        }


        try {

            keycloakAdminClient
                .deleteUser(
                    keycloakUserId
                );

        }
        catch (
            RuntimeException ignored
        ) {

            /*
             * Preserve the original
             * registration failure.
             */
        }
    }
}