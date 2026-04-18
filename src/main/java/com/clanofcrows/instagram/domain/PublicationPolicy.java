package com.clanofcrows.instagram.domain;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class PublicationPolicy {

    public PublicationSelection selectNextEligible(
            PublicationType publicationType,
            List<MediaAsset> availableMedia,
            PublicationHistory history,
            LocalDate today,
            ZoneId zoneId
    ) {
        Optional<String> cadenceRejection = rejectByCadence(publicationType, history, today, zoneId);
        if (cadenceRejection.isPresent()) {
            return PublicationSelection.skipped(cadenceRejection.get());
        }

        return availableMedia.stream()
                .filter(mediaAsset -> !history.hasSuccessfulPublication(mediaAsset))
                .findFirst()
                .map(PublicationSelection::selected)
                .orElseGet(() -> PublicationSelection.skipped("No eligible media available for " + publicationType));
    }

    public Optional<String> validateManualPublication(MediaAsset mediaAsset, PublicationHistory history) {
        if (history.hasSuccessfulPublication(mediaAsset)) {
            return Optional.of("Requested media was already published successfully: " + mediaAsset.fileName());
        }

        return Optional.empty();
    }

    private Optional<String> rejectByCadence(
            PublicationType publicationType,
            PublicationHistory history,
            LocalDate today,
            ZoneId zoneId
    ) {
        return history.latestSuccessfulPublicationAt(publicationType)
                .flatMap(lastPublication -> {
                    LocalDate lastPublicationDate = lastPublication.atZoneSameInstant(zoneId).toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(lastPublicationDate, today);
                    boolean allowed = switch (publicationType) {
                        case STORY -> daysBetween >= 1;
                        case FEED_POST -> daysBetween >= 2;
                    };
                    if (allowed) {
                        return Optional.empty();
                    }

                    return Optional.of(
                            "Automatic publication blocked by cadence rule. Last "
                                    + publicationType
                                    + " success was at "
                                    + lastPublication
                    );
                });
    }
}
