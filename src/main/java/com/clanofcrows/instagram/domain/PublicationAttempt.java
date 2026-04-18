package com.clanofcrows.instagram.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public record PublicationAttempt(
        MediaAsset mediaAsset,
        OffsetDateTime startedAt,
        String containerId
) {

    public PublicationAttempt {
        Objects.requireNonNull(mediaAsset, "mediaAsset must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
    }

    public static PublicationAttempt start(MediaAsset mediaAsset, OffsetDateTime startedAt) {
        return new PublicationAttempt(mediaAsset, startedAt, null);
    }

    public PublicationAttempt withContainerId(String containerId) {
        return new PublicationAttempt(mediaAsset, startedAt, containerId);
    }

    public PublicationHistoryEntry markSucceeded(String publishId, OffsetDateTime executedAt) {
        return new PublicationHistoryEntry(
                mediaAsset.publicationType(),
                mediaAsset.fileName(),
                mediaAsset.path().toString(),
                PublicationStatus.SUCCESS,
                executedAt,
                containerId,
                publishId,
                "Publication succeeded"
        );
    }

    public PublicationHistoryEntry markFailed(String reason, OffsetDateTime executedAt) {
        return new PublicationHistoryEntry(
                mediaAsset.publicationType(),
                mediaAsset.fileName(),
                mediaAsset.path().toString(),
                PublicationStatus.FAILURE,
                executedAt,
                containerId,
                null,
                reason
        );
    }
}
