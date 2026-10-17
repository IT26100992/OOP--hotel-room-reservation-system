
package com.hotel.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
//import java.util.List;

public class JsonFileStorage<T> {

    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    .findAndRegisterModules()
                    .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path filePath;
    private final TypeReference<T> typeReference;

    public JsonFileStorage(
            String fileName,
            TypeReference<T> typeReference) {

        this.filePath = Path.of("data", fileName);
        this.typeReference = typeReference;

        initializeFile();
    }

    private void initializeFile() {
        try {
            Files.createDirectories(filePath.getParent());

            if (Files.notExists(filePath)) {
                Files.writeString(filePath, "[]");;
            }
        } catch (IOException e) {
            throw new StorageException(
                    "Could not initialize file: " + filePath, e
            );
        }
    }

    public synchronized T read() {
        try {
            return MAPPER.readValue(
                    filePath.toFile(), typeReference
            );
        } catch (IOException e) {
            throw new StorageException(
                    "Could not read file: " + filePath, e
            );
        }
    }

    public synchronized void write(T data) {
        Path tempFile = null;

        try {
            Files.createDirectories(filePath.getParent());

            tempFile = Files.createTempFile(
                    filePath.getParent(), "hotel-", ".tmp"
            );

            MAPPER.writeValue(tempFile.toFile(), data);

            try {
                Files.move(
                        tempFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveFailure) {
                Files.move(
                        tempFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {
            throw new StorageException(
                    "Could not write file: " + filePath, e
            );
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // The main storage operation has already finished.
                }
            }
        }
    }
}
