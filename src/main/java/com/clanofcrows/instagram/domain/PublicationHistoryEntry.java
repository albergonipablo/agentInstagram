package com.clanofcrows.instagram.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.Objects;

public record PublicationHistoryEntry(
        PublicationType publicationType,
        String fileName,
        String filePath,
        PublicationStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING) OffsetDateTime executedAt,
        String containerId,
        String publishId,
        String message
) {

    public PublicationHistoryEntry {
        Objects.requireNonNull(publicationType, "publicationType must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(filePath, "filePath must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(executedAt, "executedAt must not be null");
    }

    public boolean isSuccess() {
        return PublicationStatus.SUCCESS.equals(status);
    }
}
