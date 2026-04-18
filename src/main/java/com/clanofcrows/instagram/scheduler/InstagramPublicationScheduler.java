package com.clanofcrows.instagram.scheduler;

import com.clanofcrows.instagram.application.PublishFeedPostUseCase;
import com.clanofcrows.instagram.application.PublishStoryUseCase;
import com.clanofcrows.instagram.config.InstagramProperties;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "instagram.scheduler", name = "enabled", havingValue = "true")
public class InstagramPublicationScheduler {

    private final PublishStoryUseCase publishStoryUseCase;
    private final PublishFeedPostUseCase publishFeedPostUseCase;
    private final InstagramProperties properties;

    @Scheduled(cron = "${instagram.scheduler.story-cron}")
    public void publishDailyStory() {
        executeSafely("story", publishStoryUseCase::publishNext);
    }

    @Scheduled(cron = "${instagram.scheduler.feed-cron}")
    public void publishFeedEveryOtherDay() {
        executeSafely("feed", publishFeedPostUseCase::publishNext);
    }

    private void executeSafely(String channel, Supplier<?> action) {
        try {
            log.info("Running scheduled {} publication. schedulerEnabled={}", channel, properties.getScheduler().isEnabled());
            action.get();
        } catch (RuntimeException exception) {
            log.error("Scheduled {} publication failed. Next retry will happen on the next cron cycle.", channel, exception);
        }
    }
}
