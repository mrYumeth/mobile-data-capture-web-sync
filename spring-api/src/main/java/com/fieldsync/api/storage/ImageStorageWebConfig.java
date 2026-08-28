package com.fieldsync.api.storage;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation
    .ResourceHandlerRegistry;

import org.springframework.web.servlet.config.annotation
    .WebMvcConfigurer;

import java.nio.file.Path;


@Configuration
public class ImageStorageWebConfig
        implements WebMvcConfigurer {

    private final Path localDirectory;


    public ImageStorageWebConfig(

            @Value(
                "${fieldsync.storage.local.directory:uploads/captured-images}"
            )
            String localDirectory
    ) {

        this.localDirectory =
            Path.of(
                localDirectory
            )
            .toAbsolutePath()
            .normalize();
    }


    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        String resourceLocation =
            localDirectory
                .toUri()
                .toString();


        if (
            !resourceLocation.endsWith("/")
        ) {

            resourceLocation += "/";
        }


        registry
            .addResourceHandler(
                "/uploads/captured-images/**"
            )
            .addResourceLocations(
                resourceLocation
            );
    }
}