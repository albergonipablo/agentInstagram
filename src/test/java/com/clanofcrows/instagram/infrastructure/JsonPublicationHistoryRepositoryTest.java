package com.clanofcrows.instagram.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.clanofcrows.instagram.config.InstagramProperties;
import com.clanofcrows.instagram.domain.PublicationHistoryEntry;
import com.clanofcrows.instagram.domain.PublicationStatus;
import com.clanofcrows.instagram.domain.PublicationType;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class JsonPublicationHistoryRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistAndReadHistoryEntries() {
        Path historyFile = tempDir.resolve("data/publication-history.json");
        JsonPublicationHistoryRepository repository = new JsonPublicationHistoryRepository(
                JsonMapper.builder().findAndAddModules().build(),
                properties(historyFile)
        );

        repository.append(new PublicationHistoryEntry(
                PublicationType.STORY,
                "story-1.jpg",
                "/tmp/story-1.jpg",
                PublicationStatus.SUCCESS,
                OffsetDateTime.parse("2026-04-18T09:00:00-03:00"),
                "container-1",
                "publish-1",
                "ok"
        ));

        assertThat(repository.getHistory().entries()).hasSize(1);
        assertThat(repository.getHistory().entries().getFirst().fileName()).isEqualTo("story-1.jpg");
    }

    private InstagramProperties properties(Path historyFile) {
        InstagramProperties properties = new InstagramProperties();
        InstagramProperties.History history = new InstagramProperties.History();
        history.setFile(historyFile);
        properties.setHistory(history);
        properties.setAccessToken("token");
        properties.setIgUserId("123");
        return properties;
    }
}
