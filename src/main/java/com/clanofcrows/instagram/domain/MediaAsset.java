package com.clanofcrows.instagram.domain;

import java.nio.file.Path;
import java.util.Objects;

public record MediaAsset(Path path, String fileName, PublicationType publicationType) {

    public MediaAsset {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(publicationType, "publicationType must not be null");
    }
}
