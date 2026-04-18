package com.clanofcrows.instagram.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.clanofcrows.instagram.config.InstagramProperties;
import com.clanofcrows.instagram.domain.PublicationType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalMediaRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldListSupportedFilesSortedAndIgnoreUnsupportedFiles() throws IOException {
        Path storiesDir = Files.createDirectories(tempDir.resolve("stories"));
        Files.writeString(storiesDir.resolve("b.jpg"), "b");
        Files.writeString(storiesDir.resolve("a.png"), "a");
        Files.writeString(storiesDir.resolve("ignore.txt"), "x");

        LocalMediaRepository repository = new LocalMediaRepository(properties(storiesDir, tempDir.resolve("posts")));

        assertThat(repository.listAvailableMedia(PublicationType.STORY))
                .extracting(mediaAsset -> mediaAsset.fileName())
                .containsExactly("a.png", "b.jpg");
    }

    @Test
    void shouldRejectTraversalWhenFindingFile() throws IOException {
        Path postsDir = Files.createDirectories(tempDir.resolve("posts"));
        Files.writeString(postsDir.resolve("post.jpg"), "x");

        LocalMediaRepository repository = new LocalMediaRepository(properties(tempDir.resolve("stories"), postsDir));

        assertThat(repository.findByFileName(PublicationType.FEED_POST, "../post.jpg")).isEmpty();
    }

    private InstagramProperties properties(Path storiesDir, Path postsDir) {
        InstagramProperties properties = new InstagramProperties();
        InstagramProperties.Media media = new InstagramProperties.Media();
        media.setStoriesDir(storiesDir);
        media.setPostsDir(postsDir);
        properties.setMedia(media);
        properties.setAccessToken("token");
        properties.setIgUserId("123");
        return properties;
    }
}
