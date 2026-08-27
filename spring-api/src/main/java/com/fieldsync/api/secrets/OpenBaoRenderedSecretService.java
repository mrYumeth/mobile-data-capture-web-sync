package com.fieldsync.api.secrets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Profile("local")
@Service
public class OpenBaoRenderedSecretService {

    private final Path verificationFile;

    public OpenBaoRenderedSecretService(
            @Value(
                "${fieldsync.openbao.verification-file}"
            )
            String verificationFile
    ) {

        this.verificationFile =
            Path.of(verificationFile);
    }

    public String readVerificationSecret() {

        try {

            if (
                !Files.exists(
                    verificationFile
                )
            ) {
                throw new IllegalStateException(
                    "OpenBao Agent rendered verification file does not exist"
                );
            }

            return Files
                .readString(
                    verificationFile
                )
                .trim();

        }
        catch (IOException exception) {

            throw new IllegalStateException(
                "Unable to read OpenBao Agent rendered verification secret",
                exception
            );
        }
    }
}