package com.clanofcrows.instagram.infrastructure;

import com.clanofcrows.instagram.application.PublicationGatewayException;
import com.clanofcrows.instagram.application.port.InstagramGraphClient;
import com.clanofcrows.instagram.application.port.PublicationGateway;
import com.clanofcrows.instagram.config.InstagramProperties;
import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationType;
import com.clanofcrows.instagram.publication.PublicationExecutionException;
import com.clanofcrows.instagram.publication.PublicationRequestException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetaInstagramPublicationGateway implements PublicationGateway {

    private final InstagramGraphClient instagramGraphClient;
    private final PublicMediaUrlResolver publicMediaUrlResolver;
    private final MetaTokenProvider metaTokenProvider;
    private final InstagramProperties properties;
    private final Clock clock;

    @Override
    public PublicationReceipt publish(MediaAsset mediaAsset) {
        if (Files.notExists(mediaAsset.path()) || !Files.isRegularFile(mediaAsset.path())) {
            throw new PublicationRequestException("Media file does not exist: " + mediaAsset.path());
        }

        String imageUrl = publicMediaUrlResolver.resolve(mediaAsset.publicationType(), mediaAsset.path());
        log.info("Creating media container for type={} file={} imageUrl={}",
                mediaAsset.publicationType(), mediaAsset.fileName(), imageUrl);

        String containerId = null;
        try {
            containerId = instagramGraphClient.createMediaContainer(new InstagramGraphClient.CreateMediaContainerRequest(
                    metaTokenProvider.igUserId(),
                    metaTokenProvider.accessToken(),
                    imageUrl,
                    null,
                    toMetaMediaType(mediaAsset.publicationType())
            ));

            waitUntilContainerIsFinished(mediaAsset, containerId);
            String publishId = instagramGraphClient.publishMediaContainer(
                    metaTokenProvider.igUserId(),
                    containerId,
                    metaTokenProvider.accessToken()
            );
            return new PublicationReceipt(containerId, publishId);
        } catch (RuntimeException exception) {
            throw new PublicationGatewayException(
                    "Failed to publish "
                            + mediaAsset.publicationType()
                            + " using file "
                            + mediaAsset.fileName()
                            + ": "
                            + exception.getMessage(),
                    containerId,
                    exception
            );
        }
    }

    private String toMetaMediaType(PublicationType publicationType) {
        return publicationType == PublicationType.STORY ? "STORIES" : null;
    }

    private void waitUntilContainerIsFinished(MediaAsset mediaAsset, String containerId) {
        Duration timeout = properties.getGraph().getStatusTimeout();
        Duration pollInterval = properties.getGraph().getStatusPollInterval();
        long deadline = clock.millis() + timeout.toMillis();

        while (clock.millis() <= deadline) {
            InstagramGraphClient.MediaContainerStatus status = instagramGraphClient.getMediaContainerStatus(
                    containerId,
                    metaTokenProvider.accessToken()
            );

            log.info("Container status polled type={} file={} containerId={} statusCode={} status={}",
                    mediaAsset.publicationType(), mediaAsset.fileName(), containerId, status.statusCode(), status.status());

            if (status.isFinished()) {
                return;
            }

            if (status.isFailed()) {
                throw new PublicationRequestException("Container processing failed with status " + status.statusCode());
            }

            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new PublicationExecutionException("Container polling interrupted", exception);
            }
        }

        throw new PublicationRequestException("Timed out waiting for media container to finish: " + containerId);
    }
}
