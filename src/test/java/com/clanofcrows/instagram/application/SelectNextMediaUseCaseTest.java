package com.clanofcrows.instagram.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clanofcrows.instagram.application.port.MediaCatalog;
import com.clanofcrows.instagram.application.port.PublicationHistoryRepository;
import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationHistory;
import com.clanofcrows.instagram.domain.PublicationHistoryEntry;
import com.clanofcrows.instagram.domain.PublicationPolicy;
import com.clanofcrows.instagram.domain.PublicationSelection;
import com.clanofcrows.instagram.domain.PublicationStatus;
import com.clanofcrows.instagram.domain.PublicationType;
import com.clanofcrows.instagram.publication.PublicationRequestException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SelectNextMediaUseCaseTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-18T10:15:30Z"), ZoneId.of("America/Sao_Paulo"));

    @Test
    void shouldSelectFirstUnpublishedMediaInDeterministicOrder() {
        InMemoryMediaCatalog mediaCatalog = new InMemoryMediaCatalog(List.of(
                new MediaAsset(Path.of("b.jpg"), "b.jpg", PublicationType.FEED_POST),
                new MediaAsset(Path.of("c.jpg"), "c.jpg", PublicationType.FEED_POST)
        ));
        InMemoryPublicationHistoryRepository historyRepository = new InMemoryPublicationHistoryRepository(new PublicationHistory(List.of(
                successfulEntry(PublicationType.FEED_POST, "b.jpg", "2026-04-10T10:00:00-03:00")
        )));

        SelectNextMediaUseCase useCase = new SelectNextMediaUseCase(mediaCatalog, historyRepository, new PublicationPolicy(), clock);
        PublicationSelection result = useCase.selectNext(PublicationType.FEED_POST);

        assertThat(result.hasMedia()).isTrue();
        assertThat(result.mediaAsset()).contains(new MediaAsset(Path.of("c.jpg"), "c.jpg", PublicationType.FEED_POST));
    }

    @Test
    void shouldSkipStoryWhenStoryAlreadyPublishedToday() {
        InMemoryMediaCatalog mediaCatalog = new InMemoryMediaCatalog(List.of(
                new MediaAsset(Path.of("story-2.jpg"), "story-2.jpg", PublicationType.STORY)
        ));
        InMemoryPublicationHistoryRepository historyRepository = new InMemoryPublicationHistoryRepository(new PublicationHistory(List.of(
                successfulEntry(PublicationType.STORY, "story-1.jpg", "2026-04-18T09:00:00-03:00")
        )));

        SelectNextMediaUseCase useCase = new SelectNextMediaUseCase(mediaCatalog, historyRepository, new PublicationPolicy(), clock);
        PublicationSelection result = useCase.selectNext(PublicationType.STORY);

        assertThat(result.hasMedia()).isFalse();
        assertThat(result.reason()).contains("cadence rule");
    }

    @Test
    void shouldSkipFeedWhenLastSuccessfulPublicationWasYesterday() {
        InMemoryMediaCatalog mediaCatalog = new InMemoryMediaCatalog(List.of(
                new MediaAsset(Path.of("feed-2.jpg"), "feed-2.jpg", PublicationType.FEED_POST)
        ));
        InMemoryPublicationHistoryRepository historyRepository = new InMemoryPublicationHistoryRepository(new PublicationHistory(List.of(
                successfulEntry(PublicationType.FEED_POST, "feed-1.jpg", "2026-04-17T08:30:00-03:00")
        )));

        SelectNextMediaUseCase useCase = new SelectNextMediaUseCase(mediaCatalog, historyRepository, new PublicationPolicy(), clock);
        PublicationSelection result = useCase.selectNext(PublicationType.FEED_POST);

        assertThat(result.hasMedia()).isFalse();
        assertThat(result.reason()).contains("cadence rule");
    }

    @Test
    void shouldRejectRequestedFileThatWasAlreadyPublished() {
        InMemoryMediaCatalog mediaCatalog = new InMemoryMediaCatalog(List.of(
                new MediaAsset(Path.of("feed-1.jpg"), "feed-1.jpg", PublicationType.FEED_POST)
        ));
        InMemoryPublicationHistoryRepository historyRepository = new InMemoryPublicationHistoryRepository(new PublicationHistory(List.of(
                successfulEntry(PublicationType.FEED_POST, "feed-1.jpg", "2026-04-10T08:30:00-03:00")
        )));

        SelectNextMediaUseCase useCase = new SelectNextMediaUseCase(mediaCatalog, historyRepository, new PublicationPolicy(), clock);

        assertThatThrownBy(() -> useCase.resolveRequested(PublicationType.FEED_POST, "feed-1.jpg"))
                .isInstanceOf(PublicationRequestException.class)
                .hasMessageContaining("already published");
    }

    private PublicationHistoryEntry successfulEntry(PublicationType type, String fileName, String executedAt) {
        return new PublicationHistoryEntry(
                type,
                fileName,
                fileName,
                PublicationStatus.SUCCESS,
                OffsetDateTime.parse(executedAt),
                "container",
                "publish",
                "ok"
        );
    }

    private record InMemoryMediaCatalog(List<MediaAsset> mediaAssets) implements MediaCatalog {

        @Override
        public List<MediaAsset> listAvailableMedia(PublicationType type) {
            return mediaAssets.stream().filter(mediaAsset -> mediaAsset.publicationType() == type).toList();
        }

        @Override
        public Optional<MediaAsset> findByFileName(PublicationType type, String fileName) {
            return mediaAssets.stream()
                    .filter(mediaAsset -> mediaAsset.publicationType() == type)
                    .filter(mediaAsset -> mediaAsset.fileName().equals(fileName))
                    .findFirst();
        }
    }

    private static final class InMemoryPublicationHistoryRepository implements PublicationHistoryRepository {

        private PublicationHistory history;

        private InMemoryPublicationHistoryRepository(PublicationHistory history) {
            this.history = history;
        }

        @Override
        public PublicationHistory getHistory() {
            return history;
        }

        @Override
        public void append(PublicationHistoryEntry entry) {
            history = history.withEntry(entry);
        }
    }
}
