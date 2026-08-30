package com.fieldsync.api.capturedrecord;

import com.fieldsync.api.domain.entity.CategoryEntity;
import com.fieldsync.api.domain.entity.CapturedImageEntity;
import com.fieldsync.api.domain.entity.CapturedRecordEntity;
import com.fieldsync.api.domain.entity.CustomerEntity;
import com.fieldsync.api.domain.entity.LocationEntity;
import com.fieldsync.api.domain.entity.TenantEntity;

import com.fieldsync.api.domain.repository.CategoryRepository;
import com.fieldsync.api.domain.repository.CapturedImageRepository;
import com.fieldsync.api.domain.repository.CapturedRecordRepository;
import com.fieldsync.api.domain.repository.CustomerRepository;
import com.fieldsync.api.domain.repository.LocationRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.storage.ImageStorageService;
import com.fieldsync.api.storage.StoredImage;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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

    private final CustomerRepository
        customerRepository;

    private final LocationRepository
        locationRepository;

    private final CategoryRepository
        categoryRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;

    private final ImageStorageService
        imageStorageService;


    public CapturedRecordService(
            CapturedRecordRepository capturedRecordRepository,
            CapturedImageRepository capturedImageRepository,
            CustomerRepository customerRepository,
            LocationRepository locationRepository,
            CategoryRepository categoryRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor,
            ImageStorageService imageStorageService
    ) {

        this.capturedRecordRepository =
            capturedRecordRepository;

        this.capturedImageRepository =
            capturedImageRepository;

        this.customerRepository =
            customerRepository;

        this.locationRepository =
            locationRepository;

        this.categoryRepository =
            categoryRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;

        this.imageStorageService =
            imageStorageService;
    }


    // =====================================================
    // Read
    // =====================================================

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


    // =====================================================
    // Create
    // =====================================================

    public CapturedRecordCreateResponse
    createCapturedRecord(
            CapturedRecordCreateRequest request,
            List<MultipartFile> imageFiles,
            MultipartFile legacyImage,
            String baseUrl
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        Integer tenantId =
            currentUser.tenantId();


        Integer customerId =
            parsePositiveInteger(
                request == null
                    ? null
                    : request.customerId()
            );

        Integer locationId =
            parsePositiveInteger(
                request == null
                    ? null
                    : request.locationId()
            );

        Integer categoryId =
            parsePositiveInteger(
                request == null
                    ? null
                    : request.categoryId()
            );


        if (
            customerId == null ||
            locationId == null ||
            categoryId == null
        ) {

            throw new CapturedRecordApiException(
                HttpStatus.BAD_REQUEST,
                "Customer, location and category are required"
            );
        }


        BigDecimal latitude =
            parseOptionalDecimal(
                request.latitude()
            );

        BigDecimal longitude =
            parseOptionalDecimal(
                request.longitude()
            );


        LocalDateTime capturedAt =
            parseCapturedAt(
                request.capturedAt()
            );


        String description =
            normalizeDescription(
                request.description()
            );


        // Validate all referenced records before
        // uploading any files.

        boolean referencesAreValid =
            tenantContextExecutor.execute(
                tenantId,
                () ->
                    customerRepository
                        .findByIdAndTenant_Id(
                            customerId,
                            tenantId
                        )
                        .isPresent()

                    &&

                    locationRepository
                        .findByIdAndTenant_Id(
                            locationId,
                            tenantId
                        )
                        .isPresent()

                    &&

                    categoryRepository
                        .findByIdAndTenant_Id(
                            categoryId,
                            tenantId
                        )
                        .isPresent()
            );


        if (!referencesAreValid) {

            throw new CapturedRecordApiException(
                HttpStatus.BAD_REQUEST,
                "Selected customer, location, or category does not belong to your tenant"
            );
        }


        List<MultipartFile> files =
            combineImageFiles(
                imageFiles,
                legacyImage
            );


        List<StoredImage> storedImages =
            new ArrayList<>();


        for (MultipartFile file : files) {

            storedImages.add(
                imageStorageService.store(
                    file,
                    tenantId
                )
            );
        }


        StoredImage primaryImage =
            storedImages.isEmpty()
                ? null
                : storedImages.getFirst();


        String primaryImageUrl =
            primaryImage == null
                ? null
                : primaryImage.imageUrl();

        String primaryImagePath =
            primaryImage == null
                ? null
                : primaryImage.storagePath();


        CapturedRecordResponse created =
            tenantContextExecutor.execute(
                tenantId,
                () -> {

                    CustomerEntity customer =
                        customerRepository
                            .findByIdAndTenant_Id(
                                customerId,
                                tenantId
                            )
                            .orElseThrow(
                                this::invalidReferences
                            );


                    LocationEntity location =
                        locationRepository
                            .findByIdAndTenant_Id(
                                locationId,
                                tenantId
                            )
                            .orElseThrow(
                                this::invalidReferences
                            );


                    CategoryEntity category =
                        categoryRepository
                            .findByIdAndTenant_Id(
                                categoryId,
                                tenantId
                            )
                            .orElseThrow(
                                this::invalidReferences
                            );


                    TenantEntity tenant =
                        customer.getTenant();


                    CapturedRecordEntity record =
                        CapturedRecordEntity.create(
                            tenant,
                            customer,
                            location,
                            category,
                            description,
                            latitude,
                            longitude,
                            primaryImageUrl,
                            primaryImagePath,
                            capturedAt
                        );


                    CapturedRecordEntity savedRecord =
                        capturedRecordRepository
                            .saveAndFlush(
                                record
                            );


                    if (!storedImages.isEmpty()) {

                        List<CapturedImageEntity>
                            capturedImages =
                                storedImages
                                    .stream()
                                    .map(
                                        storedImage ->
                                            CapturedImageEntity
                                                .create(
                                                    savedRecord,
                                                    tenant,
                                                    storedImage
                                                        .imageUrl(),
                                                    storedImage
                                                        .storagePath()
                                                )
                                    )
                                    .toList();


                        capturedImageRepository
                            .saveAllAndFlush(
                                capturedImages
                            );
                    }


                    CapturedRecordEntity responseRecord =
                        capturedRecordRepository
                            .findByIdAndTenant_Id(
                                savedRecord.getId(),
                                tenantId
                            )
                            .orElseThrow(
                                () ->
                                    new IllegalStateException(
                                        "Created captured record could not be reloaded"
                                    )
                            );


                    return buildResponses(
                        List.of(
                            responseRecord
                        ),
                        tenantId,
                        baseUrl
                    )
                    .getFirst();
                }
            );


        return new CapturedRecordCreateResponse(
            "Captured record created successfully",
            created
        );
    }


    // =====================================================
    // Response mapping
    // =====================================================

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


        for (CapturedImageEntity image : images) {

            Integer recordId =
                image
                    .getCapturedRecord()
                    .getId();


            String fullImageUrl =
                imageStorageService
                    .resolveImageUrl(
                        image.getImageUrl(),
                        image.getStoragePath(),
                        baseUrl
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


        for (CapturedRecordEntity record : records) {

            List<CapturedImageResponse>
                recordImages =
                    imagesByRecord
                        .getOrDefault(
                            record.getId(),
                            List.of()
                        );


            String fullImageUrl =
                null;


            if (!recordImages.isEmpty()) {

                fullImageUrl =
                    recordImages
                        .getFirst()
                        .full_image_url();
            }


            if (fullImageUrl == null) {

                fullImageUrl =
                    imageStorageService
                        .resolveImageUrl(
                            record.getImageUrl(),
                            record.getImagePath(),
                            baseUrl
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


    // =====================================================
    // Validation
    // =====================================================

    private Integer parsePositiveInteger(
            String value
    ) {

        if (
            value == null ||
            value.isBlank()
        ) {
            return null;
        }


        try {

            BigDecimal number =
                new BigDecimal(
                    value.trim()
                );


            int parsed =
                number.intValueExact();


            return parsed > 0
                ? parsed
                : null;
        }
        catch (
            NumberFormatException |
            ArithmeticException exception
        ) {

            return null;
        }
    }


    private BigDecimal parseOptionalDecimal(
            String value
    ) {

        if (
            value == null ||
            value.isEmpty()
        ) {
            return null;
        }


        try {

            return new BigDecimal(
                value
            );
        }
        catch (NumberFormatException exception) {

            throw new CapturedRecordApiException(
                HttpStatus.BAD_REQUEST,
                "Latitude and longitude must be valid numbers"
            );
        }
    }


    private LocalDateTime parseCapturedAt(
            String value
    ) {

        if (
            value == null ||
            value.isEmpty()
        ) {

            return LocalDateTime.now();
        }


        try {

            return LocalDateTime.parse(
                value
            );
        }
        catch (RuntimeException ignored) {
            // Try offset ISO timestamp.
        }


        try {

            return OffsetDateTime
                .parse(value)
                .toLocalDateTime();
        }
        catch (RuntimeException ignored) {
            // Try UTC instant below.
        }


        try {

            return Instant
                .parse(value)
                .atOffset(
                    ZoneOffset.UTC
                )
                .toLocalDateTime();
        }
        catch (RuntimeException exception) {

            throw new CapturedRecordApiException(
                HttpStatus.BAD_REQUEST,
                "Captured date/time is invalid"
            );
        }
    }


    private String normalizeDescription(
            String description
    ) {

        if (
            description == null ||
            description.isEmpty()
        ) {

            return "";
        }

        return description;
    }


    private List<MultipartFile>
    combineImageFiles(
            List<MultipartFile> images,
            MultipartFile legacyImage
    ) {

        List<MultipartFile> files =
            new ArrayList<>();


        if (images != null) {

            if (images.size() > 10) {

                throw new CapturedRecordApiException(
                    HttpStatus.BAD_REQUEST,
                    "A maximum of 10 images is allowed"
                );
            }


            files.addAll(
                images
            );
        }


        if (legacyImage != null) {

            files.add(
                legacyImage
            );
        }


        return files;
    }


    private CapturedRecordApiException
    invalidReferences() {

        return new CapturedRecordApiException(
            HttpStatus.BAD_REQUEST,
            "Selected customer, location, or category does not belong to your tenant"
        );
    }
}