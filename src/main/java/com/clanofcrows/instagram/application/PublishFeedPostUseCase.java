package com.clanofcrows.instagram.application;

import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationSelection;
import com.clanofcrows.instagram.domain.PublicationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublishFeedPostUseCase {

    private final SelectNextMediaUseCase selectNextMediaUseCase;
    private final PublicationApplicationService publicationApplicationService;

    public PublicationExecutionResult publishNext() {
        PublicationSelection selectionResult = selectNextMediaUseCase.selectNext(PublicationType.FEED_POST);
        if (!selectionResult.hasMedia()) {
            log.warn("Skipping scheduled feed publication: {}", selectionResult.reason());
            return PublicationExecutionResult.skipped(PublicationType.FEED_POST, selectionResult.reason());
        }

        MediaAsset mediaAsset = selectionResult.mediaAsset().orElseThrow();
        return publicationApplicationService.publish(mediaAsset);
    }

    public PublicationExecutionResult publish(String fileName) {
        MediaAsset mediaAsset = selectNextMediaUseCase.resolveRequested(PublicationType.FEED_POST, fileName);
        return publicationApplicationService.publish(mediaAsset);
    }
}
