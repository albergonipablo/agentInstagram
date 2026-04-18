package com.clanofcrows.instagram.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "instagram")
public class InstagramProperties {

    @NotBlank
    private String accessToken;

    @NotBlank
    private String igUserId;

    @Valid
    @NotNull
    private Media media = new Media();

    @Valid
    @NotNull
    private History history = new History();

    @Valid
    @NotNull
    private Scheduler scheduler = new Scheduler();

    @Valid
    @NotNull
    private Graph graph = new Graph();

    @Getter
    @Setter
    public static class Media {

        @NotNull
        private Path storiesDir = Path.of("./media/stories");

        @NotNull
        private Path postsDir = Path.of("./media/posts");

        private String publicBaseUrl;

    }

    @Getter
    @Setter
    public static class History {

        @NotNull
        private Path file = Path.of("./data/publication-history.json");

    }

    @Getter
    @Setter
    public static class Scheduler {

        private boolean enabled;

        @NotBlank
        private String storyCron = "0 0 10 * * *";

        @NotBlank
        private String feedCron = "0 5 10 * * *";

    }

    @Getter
    @Setter
    public static class Graph {

        @NotBlank
        private String baseUrl = "https://graph.facebook.com";

        @NotBlank
        private String apiVersion = "v24.0";

        @NotNull
        private Duration statusPollInterval = Duration.ofSeconds(5);

        @NotNull
        private Duration statusTimeout = Duration.ofMinutes(2);

    }
}
