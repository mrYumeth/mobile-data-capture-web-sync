package com.fieldsync.api.storage;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import java.net.URI;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Map;
import java.util.UUID;


@Service
public class ImageStorageService {

    private static final long
        MAX_FILE_SIZE =
            10L * 1024L * 1024L;


    private final String supabaseUrl;

    private final String serviceRoleKey;

    private final String storageBucket;

    private final long signedUrlExpiresIn;

    private final Path localDirectory;


    public ImageStorageService(

            @Value(
                "${fieldsync.storage.supabase.url:}"
            )
            String supabaseUrl,

            @Value(
                "${fieldsync.storage.supabase.service-role-key:}"
            )
            String serviceRoleKey,

            @Value(
                "${fieldsync.storage.supabase.bucket:captured-images}"
            )
            String storageBucket,

            @Value(
                "${fieldsync.storage.supabase.signed-url-expires-in:3600}"
            )
            long signedUrlExpiresIn,

            @Value(
                "${fieldsync.storage.local.directory:uploads/captured-images}"
            )
            String localDirectory
    ) {

        this.supabaseUrl =
            normalizeUrl(
                supabaseUrl
            );

        this.serviceRoleKey =
            serviceRoleKey == null
                ? ""
                : serviceRoleKey.trim();

        this.storageBucket =
            storageBucket == null ||
            storageBucket.isBlank()
                ? "captured-images"
                : storageBucket.trim();

        this.signedUrlExpiresIn =
            signedUrlExpiresIn;

        this.localDirectory =
            Path.of(
                localDirectory
            )
            .toAbsolutePath()
            .normalize();
    }


    public StoredImage store(
            MultipartFile file,
            Integer tenantId
    ) {

        if (
            tenantId == null ||
            tenantId <= 0
        ) {

            throw new ImageStorageException(
                "Tenant ID is required for image storage"
            );
        }


        if (file == null) {

            throw new ImageStorageException(
                "Image file is required"
            );
        }


        if (
            file.getSize() >
            MAX_FILE_SIZE
        ) {

            throw new ImageStorageException(
                "Image file exceeds the 10 MB limit"
            );
        }


        String fileName =
            System.currentTimeMillis()
                + "-"
                + UUID.randomUUID()
                + resolveExtension(
                    file.getOriginalFilename()
                );


        if (isSupabaseConfigured()) {

            return uploadToSupabase(
                file,
                tenantId,
                fileName
            );
        }


        return storeLocally(
            file,
            fileName
        );
    }


    public String resolveImageUrl(
            String imageUrl,
            String storagePath,
            String baseUrl
    ) {

        if (
            isSupabaseStoragePath(
                storagePath
            )
        ) {

            try {

                return createSignedUrl(
                    storagePath
                );
            }
            catch (RuntimeException exception) {

                // Preserve existing Node behaviour:
                // signed URL failure should not expose
                // the private storage path.

                return null;
            }
        }


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


        if (
            baseUrl.endsWith("/") &&
            imageUrl.startsWith("/")
        ) {

            return baseUrl.substring(
                0,
                baseUrl.length() - 1
            ) + imageUrl;
        }


        return baseUrl + imageUrl;
    }


    private StoredImage uploadToSupabase(
            MultipartFile file,
            Integer tenantId,
            String fileName
    ) {

        String storagePath =
            "tenants/"
                + tenantId
                + "/captured-records/"
                + fileName;


        URI uploadUri =
            buildObjectUri(
                null,
                storagePath
            );


        String contentType =
            file.getContentType();


        if (
            contentType == null ||
            contentType.isBlank()
        ) {

            contentType =
                MediaType
                    .APPLICATION_OCTET_STREAM_VALUE;
        }


        try {

            RestClient.create()
                .post()
                .uri(uploadUri)

                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " +
                    serviceRoleKey
                )

                .header(
                    "apikey",
                    serviceRoleKey
                )

                .header(
                    "x-upsert",
                    "false"
                )

                .contentType(
                    MediaType.parseMediaType(
                        contentType
                    )
                )

                .body(
                    file.getBytes()
                )

                .retrieve()
                .toBodilessEntity();


            return new StoredImage(
                null,
                storagePath
            );
        }
        catch (IOException exception) {

            throw new ImageStorageException(
                "Unable to read image file",
                exception
            );
        }
        catch (RuntimeException exception) {

            throw new ImageStorageException(
                "Supabase image upload failed",
                exception
            );
        }
    }


    private StoredImage storeLocally(
            MultipartFile file,
            String fileName
    ) {

        try {

            Files.createDirectories(
                localDirectory
            );


            Path target =
                localDirectory
                    .resolve(
                        fileName
                    )
                    .normalize();


            if (
                !target.startsWith(
                    localDirectory
                )
            ) {

                throw new ImageStorageException(
                    "Invalid local image path"
                );
            }


            Files.write(
                target,
                file.getBytes(),

                StandardOpenOption
                    .CREATE_NEW
            );


            return new StoredImage(

                "/uploads/captured-images/"
                    + fileName,

                target
                    .toString()
                    .replace(
                        '\\',
                        '/'
                    )
            );
        }
        catch (IOException exception) {

            throw new ImageStorageException(
                "Local image storage failed",
                exception
            );
        }
    }


    @SuppressWarnings("unchecked")
    private String createSignedUrl(
            String storagePath
    ) {

        URI signedUrlUri =
            buildObjectUri(
                "sign",
                storagePath
            );


        Map<String, Object> response =
            RestClient.create()
                .post()
                .uri(
                    signedUrlUri
                )

                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " +
                    serviceRoleKey
                )

                .header(
                    "apikey",
                    serviceRoleKey
                )

                .contentType(
                    MediaType.APPLICATION_JSON
                )

                .body(
                    Map.of(
                        "expiresIn",
                        signedUrlExpiresIn
                    )
                )

                .retrieve()
                .body(
                    Map.class
                );


        if (response == null) {

            throw new ImageStorageException(
                "Supabase signed URL response was empty"
            );
        }


        Object signedValue =
            response.get(
                "signedURL"
            );


        if (signedValue == null) {

            signedValue =
                response.get(
                    "signedUrl"
                );
        }


        if (signedValue == null) {

            throw new ImageStorageException(
                "Supabase signed URL was missing"
            );
        }


        String signedUrl =
            signedValue.toString();


        if (
            signedUrl.startsWith(
                "http://"
            ) ||
            signedUrl.startsWith(
                "https://"
            )
        ) {

            return signedUrl;
        }


        if (
            signedUrl.startsWith(
                "/storage/v1/"
            )
        ) {

            return supabaseUrl
                + signedUrl;
        }


        String storageApiUrl =
            supabaseUrl
                + "/storage/v1";


        if (
            signedUrl.startsWith("/")
        ) {

            return storageApiUrl
                + signedUrl;
        }


        return storageApiUrl
            + "/"
            + signedUrl;
    }


    private URI buildObjectUri(
            String action,
            String storagePath
    ) {

        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromUriString(
                    supabaseUrl
                )
                .pathSegment(
                    "storage",
                    "v1",
                    "object"
                );


        if (
            action != null &&
            !action.isBlank()
        ) {

            builder.pathSegment(
                action
            );
        }


        builder.pathSegment(
            storageBucket
        );


        for (
            String segment
                : storagePath.split("/")
        ) {

            if (!segment.isBlank()) {

                builder.pathSegment(
                    segment
                );
            }
        }


        return builder
            .build()
            .encode()
            .toUri();
    }


    private boolean
    isSupabaseConfigured() {

        return
            !supabaseUrl.isBlank() &&
            !serviceRoleKey.isBlank();
    }


    private boolean
    isSupabaseStoragePath(
            String storagePath
    ) {

        return
            isSupabaseConfigured() &&

            storagePath != null &&

            (
                storagePath.startsWith(
                    "captured-records/"
                ) ||

                storagePath.startsWith(
                    "tenants/"
                )
            );
    }


    private String resolveExtension(
            String originalFilename
    ) {

        if (
            originalFilename == null ||
            originalFilename.isBlank()
        ) {

            return ".jpg";
        }


        int dotIndex =
            originalFilename
                .lastIndexOf('.');


        if (
            dotIndex < 0 ||
            dotIndex ==
                originalFilename.length() - 1
        ) {

            return ".jpg";
        }


        String extension =
            originalFilename.substring(
                dotIndex
            );


        if (
            !extension.matches(
                "\\.[A-Za-z0-9]{1,10}"
            )
        ) {

            return ".jpg";
        }


        return extension;
    }


    private String normalizeUrl(
            String value
    ) {

        if (
            value == null ||
            value.isBlank()
        ) {

            return "";
        }


        String normalized =
            value.trim();


        while (
            normalized.endsWith("/")
        ) {

            normalized =
                normalized.substring(
                    0,
                    normalized.length() - 1
                );
        }


        return normalized;
    }
}