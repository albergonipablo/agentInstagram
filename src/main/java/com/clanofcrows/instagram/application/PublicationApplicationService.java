package com.clanofcrows.instagram.application;

import com.clanofcrows.instagram.application.port.PublicationGateway;
import com.clanofcrows.instagram.application.port.PublicationHistoryRepository;
import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationAttempt;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicationApplicationService {

    private final PublicationGateway publicationGateway;
    private final PublicationHistoryRepository publicationHistoryRepository;
    private final Clock clock;

    public PublicationExecutionResult publish(MediaAsset mediaAsset) {
        PublicationAttempt attempt = PublicationAttempt.start(mediaAsset, OffsetDateTime.now(clock));
        try {
            PublicationGateway.PublicationReceipt receipt = publicationGateway.publish(mediaAsset);
            PublicationAttempt completedAttempt = attempt.withContainerId(receipt.containerId());
            publicationHistoryRepository.append(completedAttempt.markSucceeded(receipt.publishId(), OffsetDateTime.now(clock)));
            log.info("Publication succeeded type={} file={} containerId={} publishId={}",
                    mediaAsset.publicationType(), mediaAsset.fileName(), receipt.containerId(), receipt.publishId());
            return PublicationExecutionResult.published(
                    mediaAsset.publicationType(),
                    mediaAsset.fileName(),
                    mediaAsset.publicationType() == com.clanofcrows.instagram.domain.PublicationType.STORY
                            ? "Story published successfully"
                            : "Feed image published successfully",
                    receipt.containerId(),
                    receipt.publishId()
            );
        } catch (PublicationGatewayException exception) {
            PublicationAttempt failedAttempt = exception.containerId() == null
                    ? attempt
                    : attempt.withContainerId(exception.containerId());
            publicationHistoryRepository.append(failedAttempt.markFailed(exception.getMessage(), OffsetDateTime.now(clock)));
            log.error("Publication failed type={} file={} containerId={} reason={}",
                    mediaAsset.publicationType(), mediaAsset.fileName(), exception.containerId(), exception.getMessage(), exception);
            throw exception.toRuntimeException();
        } catch (RuntimeException exception) {
            publicationHistoryRepository.append(attempt.markFailed(exception.getMessage(), OffsetDateTime.now(clock)));
            log.error("Publication failed type={} file={} reason={}",
                    mediaAsset.publicationType(), mediaAsset.fileName(), exception.getMessage(), exception);
            throw exception;
        }
    }
}
