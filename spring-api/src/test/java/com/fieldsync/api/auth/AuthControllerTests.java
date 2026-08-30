package com.fieldsync.api.auth;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request
    .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.status;


class AuthControllerTests {

    private CurrentUserService
        currentUserService;

    private MockMvc
        mockMvc;


    @BeforeEach
    void setUp() {

        currentUserService =
            mock(
                CurrentUserService.class
            );


        AuthController controller =
            new AuthController(
                currentUserService
            );


        mockMvc =
            MockMvcBuilders
                .standaloneSetup(
                    controller
                )
                .build();
    }


    @Test
    void shouldReturnNodeCompatibleCurrentUserPayload()
            throws Exception {

        AuthenticatedFieldSyncUser user =
            new AuthenticatedFieldSyncUser(

                25,
                7,

                "mobile-user",
                "mobile@example.test",
                "Mobile User",

                "mobile_user",

                false,
                true,

                false,

                "mobile",

                "keycloak-internal-id"
            );


        when(
            currentUserService
                .requireCurrentUser()
        )
        .thenReturn(
            user
        );


        mockMvc.perform(
                get(
                    "/api/auth/me"
                )
            )

            .andExpect(
                status().isOk()
            )

            .andExpect(
                jsonPath("$.user.id")
                    .value(25)
            )

            .andExpect(
                jsonPath("$.user.tenantId")
                    .value(7)
            )

            .andExpect(
                jsonPath("$.user.username")
                    .value(
                        "mobile-user"
                    )
            )

            .andExpect(
                jsonPath("$.user.email")
                    .value(
                        "mobile@example.test"
                    )
            )

            .andExpect(
                jsonPath("$.user.fullName")
                    .value(
                        "Mobile User"
                    )
            )

            .andExpect(
                jsonPath("$.user.role")
                    .value(
                        "mobile_user"
                    )
            )

            .andExpect(
                jsonPath("$.user.accessWeb")
                    .value(false)
            )

            .andExpect(
                jsonPath("$.user.accessMobile")
                    .value(true)
            )

            .andExpect(
                jsonPath(
                    "$.user.passwordChangeRequired"
                )
                .value(false)
            )

            .andExpect(
                jsonPath("$.user.clientType")
                    .value(
                        "mobile"
                    )
            )

            // Internal Keycloak linking ID
            // must not be exposed.
            .andExpect(
                jsonPath(
                    "$.user.keycloakUserId"
                )
                .doesNotExist()
            );


        verify(
            currentUserService
        )
        .requireCurrentUser();
    }
}