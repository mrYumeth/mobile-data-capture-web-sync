package com.fieldsync.api.capturedrecord;

import com.fieldsync.api.domain.entity.CapturedImageEntity;
import com.fieldsync.api.domain.entity.CapturedRecordEntity;

import com.fieldsync.api.domain.repository.CapturedImageRepository;
import com.fieldsync.api.domain.repository.CapturedRecordRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class CapturedRecordService {

    private final CapturedRecordRepository
        capturedRecordRepository;

    private final CapturedImageRepository
        capturedImageRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;


    public CapturedRecordService(
            CapturedRecordRepository capturedRecordRepository,
            CapturedImageRepository capturedImageRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor
    ) {

        this.capturedRecordRepository =
            capturedRecordRepository;

        this.capturedImageRepository =
            capturedImageRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;
    }


    public List<CapturedRecordResponse>
    getCapturedRecords(
            String baseUrl
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                List<CapturedRecordEntity> records =
                    capturedRecordRepository
                        .findAllByTenant_IdOrderByReceivedAtDesc(
                            tenantId
                        );

                return buildResponses(
                    records,
                    tenantId,
                    baseUrl
                );
            }
        );
    }


    public CapturedRecordResponse
    getCapturedRecord(
            Integer recordId,
            String baseUrl
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                CapturedRecordEntity record =
                    capturedRecordRepository
                        .findByIdAndTenant_Id(
                            recordId,
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new CapturedRecordApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Captured record not found"
                                )
                        );


                return buildResponses(
                    List.of(record),
                    tenantId,
                    baseUrl
                )
                .getFirst();
            }
        );
    }


    private List<CapturedRecordResponse>
    buildResponses(
            List<CapturedRecordEntity> records,
            Integer tenantId,
            String baseUrl
    ) {

        if (records.isEmpty()) {
            return List.of();
        }


        List<Integer> recordIds =
            records
                .stream()
                .map(
                    CapturedRecordEntity::getId
                )
                .toList();


        List<CapturedImageEntity> images =
            capturedImageRepository
                .findAllByCapturedRecord_IdInAndTenant_IdOrderByIdAsc(
                    recordIds,
                    tenantId
                );


        Map<Integer, List<CapturedImageResponse>>
            imagesByRecord =
                new HashMap<>();


        for (
            CapturedImageEntity image
                : images
        ) {

            Integer recordId =
                image
                    .getCapturedRecord()
                    .getId();


            String fullImageUrl =
                resolveBasicImageUrl(
                    baseUrl,
                    image.getImageUrl()
                );


            CapturedImageResponse response =
                CapturedImageResponse.from(
                    image,
                    fullImageUrl
                );


            imagesByRecord
                .computeIfAbsent(
                    recordId,
                    ignored ->
                        new ArrayList<>()
                )
                .add(response);
        }


        List<CapturedRecordResponse> responses =
            new ArrayList<>();


        for (
            CapturedRecordEntity record
                : records
        ) {

            List<CapturedImageResponse>
                recordImages =
                    imagesByRecord
                        .getOrDefault(
                            record.getId(),
                            List.of()
                        );


            String fullImageUrl = null;


            if (!recordImages.isEmpty()) {

                fullImageUrl =
                    recordImages
                        .getFirst()
                        .full_image_url();
            }


            if (fullImageUrl == null) {

                fullImageUrl =
                    resolveBasicImageUrl(
                        baseUrl,
                        record.getImageUrl()
                    );
            }


            responses.add(
                CapturedRecordResponse.from(
                    record,
                    recordImages,
                    fullImageUrl
                )
            );
        }


        return responses;
    }


    private String resolveBasicImageUrl(
            String baseUrl,
            String imageUrl
    ) {

        if (
            imageUrl == null ||
            imageUrl.isBlank()
        ) {
            return null;
        }


        if (
            imageUrl.startsWith(
                "http://"
            ) ||
            imageUrl.startsWith(
                "https://"
            )
        ) {
            return imageUrl;
        }


        if (
            baseUrl == null ||
            baseUrl.isBlank()
        ) {
            return imageUrl;
        }


        return baseUrl + imageUrl;
    }
}