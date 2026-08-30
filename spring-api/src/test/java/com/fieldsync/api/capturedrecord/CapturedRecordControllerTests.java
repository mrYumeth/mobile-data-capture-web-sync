package com.fieldsync.api.capturedrecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import static org.springframework.test.web.servlet.request
    .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request
    .MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.content;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.status;


class CapturedRecordControllerTests {

    private CapturedRecordService
        capturedRecordService;

    private MockMvc
        mockMvc;


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
                .standaloneSetup(
                    controller
                )
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


    @Test
    void shouldAcceptFlutterMultipartRequestAndReturnServerId()
            throws Exception {

        MockMultipartFile image =
            new MockMultipartFile(
                "images",
                "field-image.jpg",
                "image/jpeg",
                new byte[] {
                    1,
                    2,
                    3
                }
            );


        LocalDateTime now =
            LocalDateTime.of(
                2026,
                8,
                29,
                10,
                30
            );


        CapturedRecordResponse record =
            new CapturedRecordResponse(

                321,
                10,

                11,
                12,
                13,

                "Customer",
                "Location",
                "Category",

                "Flutter multipart",

                new BigDecimal(
                    "6.9270790"
                ),

                new BigDecimal(
                    "79.8612440"
                ),

                null,
                null,

                now,
                now,
                now,
                now,

                List.of(),

                null
            );


        when(
            capturedRecordService
                .createCapturedRecord(
                    any(
                        CapturedRecordCreateRequest.class
                    ),
                    anyList(),
                    isNull(),
                    anyString()
                )
        )
        .thenReturn(
            new CapturedRecordCreateResponse(
                "Captured record created successfully",
                record
            )
        );


        mockMvc.perform(
                multipart(
                    "/api/captured-records"
                )

                .file(image)

                .param(
                    "customer_id",
                    "11"
                )

                .param(
                    "location_id",
                    "12"
                )

                .param(
                    "category_id",
                    "13"
                )

                .param(
                    "description",
                    "Flutter multipart"
                )

                .param(
                    "latitude",
                    "6.9270790"
                )

                .param(
                    "longitude",
                    "79.8612440"
                )

                .param(
                    "captured_at",
                    "2026-08-29T10:30:00"
                )
            )

            .andExpect(
                status().isCreated()
            )

            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Captured record created successfully"
                    )
            )

            .andExpect(
                jsonPath("$.record.id")
                    .value(321)
            );


        ArgumentCaptor<CapturedRecordCreateRequest>
            requestCaptor =
                ArgumentCaptor.forClass(
                    CapturedRecordCreateRequest.class
                );


        verify(
            capturedRecordService
        )
        .createCapturedRecord(
            requestCaptor.capture(),
            anyList(),
            isNull(),
            eq("http://localhost")
        );


        CapturedRecordCreateRequest captured =
            requestCaptor.getValue();


        assertThat(captured.customerId())
            .isEqualTo("11");

        assertThat(captured.locationId())
            .isEqualTo("12");

        assertThat(captured.categoryId())
            .isEqualTo("13");

        assertThat(captured.description())
            .isEqualTo(
                "Flutter multipart"
            );

        assertThat(captured.latitude())
            .isEqualTo(
                "6.9270790"
            );

        assertThat(captured.longitude())
            .isEqualTo(
                "79.8612440"
            );
    }
}