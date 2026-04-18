package com.clanofcrows.instagram.application;

import com.clanofcrows.instagram.domain.PublicationType;

public record PublicationExecutionResult(
        PublicationExecutionStatus status,
        PublicationType publicationType,
        String fileName,
        String message,
        String containerId,
        String publishId
) {

    public static PublicationExecutionResult published(
            PublicationType publicationType,
            String fileName,
            String message,
            String containerId,
            String publishId
    ) {
        return new PublicationExecutionResult(
                PublicationExecutionStatus.PUBLISHED,
                publicationType,
                fileName,
                message,
                containerId,
                publishId
        );
    }

    public static PublicationExecutionResult skipped(PublicationType publicationType, String message) {
        return new PublicationExecutionResult(
                PublicationExecutionStatus.SKIPPED,
                publicationType,
                null,
                message,
                null,
                null
        );
    }
}
