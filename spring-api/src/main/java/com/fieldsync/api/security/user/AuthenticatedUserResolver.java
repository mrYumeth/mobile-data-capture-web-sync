package com.fieldsync.api.security.user;

import com.fieldsync.api.domain.entity.UserEntity;
import com.fieldsync.api.domain.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;

    public AuthenticatedUserResolver(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthenticatedFieldSyncUser resolve(
            Jwt jwt
    ) {

        if (jwt == null) {
            throw new AccessDeniedException(
                "Authenticated JWT is required"
            );
        }

        String keycloakUserId =
            jwt.getSubject();

        if (
            keycloakUserId == null ||
            keycloakUserId.isBlank()
        ) {
            throw new AccessDeniedException(
                "Keycloak subject is missing"
            );
        }

        UserEntity user =
            findAndLinkUser(
                jwt,
                keycloakUserId
            );

        if (
            user == null ||
            !Boolean.TRUE.equals(
                user.getActive()
            )
        ) {
            throw new AccessDeniedException(
                "Your Keycloak account is not linked to an active FieldSync user"
            );
        }

        String clientType =
            resolveClientType(jwt);

        if (
            !isClientAllowed(
                user,
                clientType
            )
        ) {
            throw new AccessDeniedException(
                "Your account is not allowed to access the "
                    + clientType
                    + " application"
            );
        }

        return new AuthenticatedFieldSyncUser(
            user.getId(),
            user.getTenant().getId(),

            user.getUsername(),
            user.getEmail(),
            user.getFullName(),

            user.getRole(),

            Boolean.TRUE.equals(
                user.getAccessWeb()
            ),

            Boolean.TRUE.equals(
                user.getAccessMobile()
            ),

            Boolean.TRUE.equals(
                user.getPasswordChangeRequired()
            ),

            clientType,

            user.getKeycloakUserId()
        );
    }

    private UserEntity findAndLinkUser(
            Jwt jwt,
            String keycloakUserId
    ) {

        var linkedUser =
            userRepository
                .findByKeycloakUserId(
                    keycloakUserId
                );

        if (linkedUser.isPresent()) {
            return linkedUser.get();
        }

        String email =
            jwt.getClaimAsString("email");

        if (
            email == null ||
            email.isBlank()
        ) {
            return null;
        }

        var emailUser =
            userRepository
                .findByEmailIgnoreCase(
                    email
                );

        if (emailUser.isEmpty()) {
            return null;
        }

        UserEntity user =
            emailUser.get();

        String existingKeycloakUserId =
            user.getKeycloakUserId();

        if (
            existingKeycloakUserId != null &&
            !existingKeycloakUserId.isBlank() &&
            !existingKeycloakUserId.equals(
                keycloakUserId
            )
        ) {
            throw new AccessDeniedException(
                "Email is already linked to another Keycloak account"
            );
        }

        if (
            existingKeycloakUserId == null ||
            existingKeycloakUserId.isBlank()
        ) {
            user.linkKeycloakUser(
                keycloakUserId
            );

            userRepository.saveAndFlush(
                user
            );
        }

        return user;
    }

    private String resolveClientType(
            Jwt jwt
    ) {

        String authorizedParty =
            jwt.getClaimAsString("azp");

        if (
            "fieldsync-mobile"
                .equals(authorizedParty)
        ) {
            return "mobile";
        }

        return "web";
    }

    private boolean isClientAllowed(
            UserEntity user,
            String clientType
    ) {

        if (
            "admin".equals(
                user.getRole()
            )
        ) {
            return true;
        }

        if (
            "mobile".equals(
                clientType
            )
        ) {
            return Boolean.TRUE.equals(
                user.getAccessMobile()
            );
        }

        return Boolean.TRUE.equals(
            user.getAccessWeb()
        );
    }
}