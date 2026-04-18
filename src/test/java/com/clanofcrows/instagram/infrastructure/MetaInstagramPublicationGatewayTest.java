package com.clanofcrows.instagram.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.clanofcrows.instagram.application.port.InstagramGraphClient;
import com.clanofcrows.instagram.application.port.PublicationGateway;
import com.clanofcrows.instagram.config.InstagramProperties;
import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationType;
import com.clanofcrows.instagram.publication.PublicationRequestException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class MetaInstagramPublicationGatewayTest {

    @TempDir
    Path tempDir;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-18T10:15:30Z"), ZoneId.of("America/Sao_Paulo"));

    @Test
    void shouldPublishFeedImageThroughMetaFlow() throws Exception {
        Path postsDir = Files.createDirectories(tempDir.resolve("posts"));
        Path image = Files.writeString(postsDir.resolve("post-1.jpg"), "test");

        InstagramGraphClient graphClient = Mockito.mock(InstagramGraphClient.class);
        PublicMediaUrlResolver urlResolver = Mockito.mock(PublicMediaUrlResolver.class);

        when(urlResolver.resolve(any(), any())).thenReturn("https://example.com/internal/media/posts/post-1.jpg");
        when(graphClient.createMediaContainer(any())).thenReturn("container-1");
        when(graphClient.getMediaContainerStatus("container-1", "token"))
                .thenReturn(new InstagramGraphClient.MediaContainerStatus("FINISHED", "FINISHED"));
        when(graphClient.publishMediaContainer("ig-user", "container-1", "token")).thenReturn("publish-1");

        MetaInstagramPublicationGateway gateway = new MetaInstagramPublicationGateway(
                graphClient,
                urlResolver,
                new MetaTokenProvider(properties(postsDir)),
                properties(postsDir),
                clock
        );

        PublicationGateway.PublicationReceipt receipt = gateway.publish(
                new MediaAsset(image.toAbsolutePath(), "post-1.jpg", PublicationType.FEED_POST)
        );

        assertThat(receipt.containerId()).isEqualTo("container-1");
        assertThat(receipt.publishId()).isEqualTo("publish-1");
    }

    @Test
    void shouldFailWhenContainerNeverFinishes() throws Exception {
        Path postsDir = Files.createDirectories(tempDir.resolve("posts"));
        Path image = Files.writeString(postsDir.resolve("post-1.jpg"), "test");

        InstagramGraphClient graphClient = Mockito.mock(InstagramGraphClient.class);
        PublicMediaUrlResolver urlResolver = Mockito.mock(PublicMediaUrlResolver.class);

        when(urlResolver.resolve(any(), any())).thenReturn("https://example.com/internal/media/posts/post-1.jpg");
        when(graphClient.createMediaContainer(any())).thenReturn("container-1");
        when(graphClient.getMediaContainerStatus("container-1", "token"))
                .thenReturn(new InstagramGraphClient.MediaContainerStatus("IN_PROGRESS", "IN_PROGRESS"));

        InstagramProperties properties = properties(postsDir);
        properties.getGraph().setStatusPollInterval(java.time.Duration.ZERO);
        properties.getGraph().setStatusTimeout(java.time.Duration.ZERO);

        MetaInstagramPublicationGateway gateway = new MetaInstagramPublicationGateway(
                graphClient,
                urlResolver,
                new MetaTokenProvider(properties),
                properties,
                new AdvancingClock(Instant.parse("2026-04-18T10:15:30Z"), ZoneId.of("America/Sao_Paulo"), Duration.ofMillis(1))
        );

        assertThatThrownBy(() -> gateway.publish(new MediaAsset(image.toAbsolutePath(), "post-1.jpg", PublicationType.FEED_POST)))
                .isInstanceOf(com.clanofcrows.instagram.application.PublicationGatewayException.class)
                .hasMessageContaining("Timed out");
    }

    private InstagramProperties properties(Path postsDir) {
        InstagramProperties properties = new InstagramProperties();
        InstagramProperties.Media media = new InstagramProperties.Media();
        media.setPostsDir(postsDir);
        media.setStoriesDir(tempDir.resolve("stories"));
        media.setPublicBaseUrl("https://example.com");
        properties.setMedia(media);
        properties.setAccessToken("token");
        properties.setIgUserId("ig-user");
        return properties;
    }

    private static final class AdvancingClock extends Clock {

        private final AtomicLong currentMillis;
        private final ZoneId zoneId;
        private final long stepMillis;

        private AdvancingClock(Instant start, ZoneId zoneId, Duration step) {
            this.currentMillis = new AtomicLong(start.toEpochMilli());
            this.zoneId = zoneId;
            this.stepMillis = step.toMillis();
        }

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new AdvancingClock(Instant.ofEpochMilli(currentMillis.get()), zone, Duration.ofMillis(stepMillis));
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(currentMillis.getAndAdd(stepMillis));
        }
    }
}
