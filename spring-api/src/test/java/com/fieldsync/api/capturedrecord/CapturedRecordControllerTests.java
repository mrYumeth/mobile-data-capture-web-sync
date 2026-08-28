package com.fieldsync.api.capturedrecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CapturedRecordControllerTests {

    private CapturedRecordService capturedRecordService;

    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {

        capturedRecordService =
            mock(
                CapturedRecordService.class
            );


        CapturedRecordController controller =
            new CapturedRecordController(
                capturedRecordService
            );


        mockMvc =
            MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }


    @Test
    void shouldRejectNonNumericCapturedRecordId()
            throws Exception {

        mockMvc.perform(
                get(
                    "/api/captured-records/abc"
                )
            )
            .andExpect(
                status().isBadRequest()
            )
            .andExpect(
                content()
                    .contentTypeCompatibleWith(
                        "application/json"
                    )
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Invalid captured record ID"
                    )
            );
    }


    @Test
    void shouldRejectNonPositiveCapturedRecordId()
            throws Exception {

        mockMvc.perform(
                get(
                    "/api/captured-records/0"
                )
            )
            .andExpect(
                status().isBadRequest()
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Invalid captured record ID"
                    )
            );
    }


    @Test
    void shouldAcceptNodeCompatibleDecimalIntegerId()
            throws Exception {

        mockMvc.perform(
                get(
                    "/api/captured-records/15.0"
                )
            )
            .andExpect(
                status().isOk()
            );


        verify(
            capturedRecordService
        )
        .getCapturedRecord(
            15,
            "http://localhost"
        );
    }
}