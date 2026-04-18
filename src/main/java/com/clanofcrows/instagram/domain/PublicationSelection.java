package com.clanofcrows.instagram.domain;

import java.util.Objects;
import java.util.Optional;

public record PublicationSelection(Optional<MediaAsset> mediaAsset, String reason) {

    public PublicationSelection {
        Objects.requireNonNull(mediaAsset, "mediaAsset must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }

    public static PublicationSelection selected(MediaAsset mediaAsset) {
        return new PublicationSelection(Optional.of(mediaAsset), "");
    }

    public static PublicationSelection skipped(String reason) {
        return new PublicationSelection(Optional.empty(), reason);
    }

    public boolean hasMedia() {
        return mediaAsset.isPresent();
    }
}
