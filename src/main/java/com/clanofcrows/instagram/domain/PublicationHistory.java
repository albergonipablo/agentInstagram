package com.clanofcrows.instagram.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PublicationHistory {

    private final List<PublicationHistoryEntry> entries;

    @JsonCreator
    public PublicationHistory(@JsonProperty("entries") List<PublicationHistoryEntry> entries) {
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static PublicationHistory empty() {
        return new PublicationHistory(List.of());
    }

    public List<PublicationHistoryEntry> entries() {
        return entries;
    }

    public PublicationHistory withEntry(PublicationHistoryEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        List<PublicationHistoryEntry> updatedEntries = new ArrayList<>(entries);
        updatedEntries.add(entry);
        return new PublicationHistory(updatedEntries);
    }

    public boolean hasSuccessfulPublication(PublicationType type, String fileName) {
        return entries.stream()
                .filter(PublicationHistoryEntry::isSuccess)
                .anyMatch(entry -> entry.publicationType() == type && entry.fileName().equalsIgnoreCase(fileName));
    }

    public boolean hasSuccessfulPublication(MediaAsset mediaAsset) {
        return hasSuccessfulPublication(mediaAsset.publicationType(), mediaAsset.fileName());
    }

    public Optional<PublicationHistoryEntry> latestSuccessfulPublication(PublicationType type) {
        return entries.stream()
                .filter(PublicationHistoryEntry::isSuccess)
                .filter(entry -> entry.publicationType() == type)
                .max(Comparator.comparing(PublicationHistoryEntry::executedAt));
    }

    public Optional<OffsetDateTime> latestSuccessfulPublicationAt(PublicationType type) {
        return latestSuccessfulPublication(type).map(PublicationHistoryEntry::executedAt);
    }
}
