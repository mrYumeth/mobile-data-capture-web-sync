package com.fieldsync.api.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class ImageStorageServiceTests {

    @TempDir
    Path tempDirectory;


    @Test
    void shouldStoreImageLocallyWhenSupabaseIsNotConfigured()
            throws Exception {

        ImageStorageService service =
            new ImageStorageService(
                "",
                "",
                "captured-images",
                3600,
                tempDirectory.toString()
            );


        MockMultipartFile image =
            new MockMultipartFile(
                "images",
                "capture.png",
                "image/png",
                new byte[] {
                    1,
                    2,
                    3,
                    4
                }
            );


        StoredImage stored =
            service.store(
                image,
                42
            );


        assertThat(
            stored.imageUrl()
        )
        .startsWith(
            "/uploads/captured-images/"
        );


        assertThat(
            stored.storagePath()
        )
        .endsWith(
            ".png"
        );


        assertThat(
            Files.exists(
                Path.of(
                    stored.storagePath()
                )
            )
        )
        .isTrue();
    }


    @Test
    void shouldResolveLocalImageUrl() {

        ImageStorageService service =
            new ImageStorageService(
                "",
                "",
                "captured-images",
                3600,
                tempDirectory.toString()
            );


        String resolved =
            service.resolveImageUrl(
                "/uploads/captured-images/test.jpg",
                null,
                "http://localhost:8081"
            );


        assertThat(resolved)
            .isEqualTo(
                "http://localhost:8081/uploads/captured-images/test.jpg"
            );
    }


    @Test
    void shouldPreserveExistingHttpImageUrl() {

        ImageStorageService service =
            new ImageStorageService(
                "",
                "",
                "captured-images",
                3600,
                tempDirectory.toString()
            );


        String resolved =
            service.resolveImageUrl(
                "https://example.test/image.jpg",
                null,
                "http://localhost:8081"
            );


        assertThat(resolved)
            .isEqualTo(
                "https://example.test/image.jpg"
            );
    }
}