package com.clanofcrows.instagram.infrastructure;

import com.clanofcrows.instagram.application.port.MediaCatalog;
import com.clanofcrows.instagram.config.InstagramProperties;
import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LocalMediaRepository implements MediaCatalog {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    private final InstagramProperties properties;

    @Override
    public List<MediaAsset> listAvailableMedia(PublicationType type) {
        Path directory = baseDirectory(type);
        if (Files.notExists(directory)) {
            log.warn("Media directory does not exist for {}: {}", type, directory.toAbsolutePath());
            return List.of();
        }

        try (Stream<Path> pathStream = Files.list(directory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .peek(path -> {
                        if (!isSupportedMediaFile(path)) {
                            log.warn("Ignoring unsupported media file: {}", path.toAbsolutePath());
                        }
                    })
                    .filter(this::isSupportedMediaFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .map(path -> new MediaAsset(path.toAbsolutePath().normalize(), path.getFileName().toString(), type))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to list media from " + directory.toAbsolutePath(), exception);
        }
    }

    @Override
    public Optional<MediaAsset> findByFileName(PublicationType type, String fileName) {
        Path baseDirectory = baseDirectory(type);
        Path candidate = baseDirectory.resolve(fileName).normalize();
        if (!candidate.startsWith(baseDirectory)) {
            return Optional.empty();
        }

        if (Files.notExists(candidate) || !Files.isRegularFile(candidate) || !isSupportedMediaFile(candidate)) {
            return Optional.empty();
        }

        return Optional.of(new MediaAsset(candidate.toAbsolutePath().normalize(), candidate.getFileName().toString(), type));
    }

    public Path baseDirectory(PublicationType type) {
        return switch (type) {
            case STORY -> properties.getMedia().getStoriesDir().toAbsolutePath().normalize();
            case FEED_POST -> properties.getMedia().getPostsDir().toAbsolutePath().normalize();
        };
    }

    private boolean isSupportedMediaFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}
