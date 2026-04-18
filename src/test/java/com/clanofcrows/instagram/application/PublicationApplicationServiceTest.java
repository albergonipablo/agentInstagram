package com.clanofcrows.instagram.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clanofcrows.instagram.application.port.PublicationGateway;
import com.clanofcrows.instagram.application.port.PublicationHistoryRepository;
import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationHistoryEntry;
import com.clanofcrows.instagram.domain.PublicationStatus;
import com.clanofcrows.instagram.domain.PublicationType;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class PublicationApplicationServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-18T10:15:30Z"), ZoneId.of("America/Sao_Paulo"));

    @Test
    void shouldPersistSuccessHistoryAfterGatewayPublish() {
        PublicationGateway publicationGateway = Mockito.mock(PublicationGateway.class);
        PublicationHistoryRepository historyRepository = Mockito.mock(PublicationHistoryRepository.class);
        MediaAsset mediaAsset = new MediaAsset(Path.of("/tmp/post-1.jpg"), "post-1.jpg", PublicationType.FEED_POST);

        when(publicationGateway.publish(mediaAsset))
                .thenReturn(new PublicationGateway.PublicationReceipt("container-1", "publish-1"));

        PublicationApplicationService service = new PublicationApplicationService(publicationGateway, historyRepository, clock);

        PublicationExecutionResult result = service.publish(mediaAsset);

        assertThat(result.status()).isEqualTo(PublicationExecutionStatus.PUBLISHED);
        assertThat(result.containerId()).isEqualTo("container-1");
        assertThat(result.publishId()).isEqualTo("publish-1");

        ArgumentCaptor<PublicationHistoryEntry> captor = ArgumentCaptor.forClass(PublicationHistoryEntry.class);
        verify(historyRepository, times(1)).append(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(PublicationStatus.SUCCESS);
        assertThat(captor.getValue().containerId()).isEqualTo("container-1");
    }

    @Test
    void shouldPersistFailureHistoryWhenGatewayFails() {
        PublicationGateway publicationGateway = Mockito.mock(PublicationGateway.class);
        PublicationHistoryRepository historyRepository = Mockito.mock(PublicationHistoryRepository.class);
        MediaAsset mediaAsset = new MediaAsset(Path.of("/tmp/story-1.jpg"), "story-1.jpg", PublicationType.STORY);

        when(publicationGateway.publish(mediaAsset)).thenThrow(new IllegalStateException("publish failed"));

        PublicationApplicationService service = new PublicationApplicationService(publicationGateway, historyRepository, clock);

        assertThatThrownBy(() -> service.publish(mediaAsset))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("publish failed");

        ArgumentCaptor<PublicationHistoryEntry> captor = ArgumentCaptor.forClass(PublicationHistoryEntry.class);
        verify(historyRepository, times(1)).append(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(PublicationStatus.FAILURE);
        assertThat(captor.getValue().message()).isEqualTo("publish failed");
    }
}
