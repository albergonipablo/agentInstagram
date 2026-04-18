package com.clanofcrows.instagram.infrastructure;

import com.clanofcrows.instagram.config.InstagramProperties;
import com.clanofcrows.instagram.domain.PublicationType;
import com.clanofcrows.instagram.publication.PublicationRequestException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component
@RequiredArgsConstructor
public class PublicMediaUrlResolver {

    private final InstagramProperties properties;
    private final LocalMediaRepository localMediaRepository;

    public String resolve(PublicationType publicationType, Path imagePath) {
        String publicBaseUrl = properties.getMedia().getPublicBaseUrl();
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new PublicationRequestException(
                    "instagram.media.public-base-url is required because Instagram Graph API consumes image_url, not local files"
            );
        }

        Path normalizedImagePath = imagePath.toAbsolutePath().normalize();
        Path baseDirectory = localMediaRepository.baseDirectory(publicationType);
        if (!normalizedImagePath.startsWith(baseDirectory)) {
            throw new PublicationRequestException(
                    "Requested media is outside the configured directory for " + publicationType + ": " + normalizedImagePath
            );
        }

        String sanitizedBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        return sanitizedBaseUrl
                + "/internal/media/"
                + publicationType.pathSegment()
                + "/"
                + UriUtils.encodePathSegment(normalizedImagePath.getFileName().toString(), java.nio.charset.StandardCharsets.UTF_8);
    }
}
