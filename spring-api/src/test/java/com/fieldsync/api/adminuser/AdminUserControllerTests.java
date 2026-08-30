package com.fieldsync.api.adminuser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request
    .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.status;


class AdminUserControllerTests {

    private AdminUserService
        adminUserService;

    private MockMvc
        mockMvc;


    @BeforeEach
    void setUp() {

        adminUserService =
            mock(
                AdminUserService.class
            );


        AdminUserController controller =
            new AdminUserController(
                adminUserService
            );


        mockMvc =
            MockMvcBuilders
                .standaloneSetup(
                    controller
                )
                .build();
    }


    @Test
    void shouldReturnNodeCompatibleUserPayload()
            throws Exception {

        LocalDateTime createdAt =
            LocalDateTime.of(
                2026,
                8,
                30,
                10,
                0
            );


        AdminUserResponse user =
            new AdminUserResponse(

                25,
                7,

                "field-user",
                "field@example.test",
                "Field User",

                "user",

                true,
                true,

                true,

                createdAt,

                false,

                "keycloak-user-id",

                createdAt
            );


        when(
            adminUserService
                .getUsers()
        )
            .thenReturn(
                List.of(
                    user
                )
            );


        mockMvc.perform(
                get(
                    "/api/admin/users"
                )
            )
            .andExpect(
                status().isOk()
            )
            .andExpect(
                jsonPath("$[0].id")
                    .value(25)
            )
            .andExpect(
                jsonPath("$[0].tenant_id")
                    .value(7)
            )
            .andExpect(
                jsonPath("$[0].username")
                    .value("field-user")
            )
            .andExpect(
                jsonPath("$[0].full_name")
                    .value("Field User")
            )
            .andExpect(
                jsonPath("$[0].access_web")
                    .value(true)
            )
            .andExpect(
                jsonPath("$[0].access_mobile")
                    .value(true)
            )
            .andExpect(
                jsonPath("$[0].is_active")
                    .value(true)
            )
            .andExpect(
                jsonPath("$[0].keycloak_user_id")
                    .value(
                        "keycloak-user-id"
                    )
            );
    }


    @Test
    void shouldReturnForbiddenForNonAdmin()
            throws Exception {

        when(
            adminUserService
                .getUsers()
        )
            .thenThrow(
                new AdminUserApiException(
                    HttpStatus.FORBIDDEN,
                    "Admin access is required"
                )
            );


        mockMvc.perform(
                get(
                    "/api/admin/users"
                )
            )
            .andExpect(
                status().isForbidden()
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Admin access is required"
                    )
            );
    }
}