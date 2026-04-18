package com.clanofcrows.instagram.application;

import com.clanofcrows.instagram.application.port.MediaCatalog;
import com.clanofcrows.instagram.application.port.PublicationHistoryRepository;
import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationHistory;
import com.clanofcrows.instagram.domain.PublicationPolicy;
import com.clanofcrows.instagram.domain.PublicationSelection;
import com.clanofcrows.instagram.domain.PublicationType;
import com.clanofcrows.instagram.publication.PublicationRequestException;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SelectNextMediaUseCase {

    private final MediaCatalog mediaCatalog;
    private final PublicationHistoryRepository publicationHistoryRepository;
    private final PublicationPolicy publicationPolicy;
    private final Clock clock;

    public PublicationSelection selectNext(PublicationType publicationType) {
        PublicationHistory history = publicationHistoryRepository.getHistory();
        return publicationPolicy.selectNextEligible(
                publicationType,
                mediaCatalog.listAvailableMedia(publicationType),
                history,
                LocalDate.now(clock),
                clock.getZone()
        );
    }

    public MediaAsset resolveRequested(PublicationType publicationType, String fileName) {
        PublicationHistory history = publicationHistoryRepository.getHistory();
        MediaAsset mediaAsset = mediaCatalog.findByFileName(publicationType, fileName)
                .orElseThrow(() -> new PublicationRequestException(
                        "Requested media does not exist or is invalid: " + fileName
                ));

        publicationPolicy.validateManualPublication(mediaAsset, history)
                .ifPresent(reason -> {
                    throw new PublicationRequestException(reason);
                });

        return mediaAsset;
    }
}
