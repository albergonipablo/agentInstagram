package com.clanofcrows.instagram.infrastructure;

import com.clanofcrows.instagram.application.port.PublicationHistoryRepository;
import com.clanofcrows.instagram.config.InstagramProperties;
import com.clanofcrows.instagram.domain.PublicationHistory;
import com.clanofcrows.instagram.domain.PublicationHistoryEntry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Repository
public class JsonPublicationHistoryRepository implements PublicationHistoryRepository {

    private final ObjectMapper objectMapper;
    private final Path historyFile;
    private final ReentrantLock lock = new ReentrantLock();

    public JsonPublicationHistoryRepository(ObjectMapper objectMapper, InstagramProperties properties) {
        this.objectMapper = objectMapper;
        this.historyFile = properties.getHistory().getFile().toAbsolutePath().normalize();
    }

    @Override
    public PublicationHistory getHistory() {
        lock.lock();
        try {
            return readHistory();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void append(PublicationHistoryEntry entry) {
        lock.lock();
        try {
            PublicationHistory updatedHistory = readHistory().withEntry(entry);
            writeHistory(updatedHistory);
        } finally {
            lock.unlock();
        }
    }

    private PublicationHistory readHistory() {
        if (Files.notExists(historyFile)) {
            return PublicationHistory.empty();
        }

        try {
            return objectMapper.readValue(historyFile.toFile(), PublicationHistory.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to read publication history from " + historyFile, exception);
        }
    }

    private void writeHistory(PublicationHistory publicationHistory) {
        try {
            Files.createDirectories(historyFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(historyFile.toFile(), publicationHistory);
        } catch (JacksonException | IOException exception) {
            throw new IllegalStateException("Unable to write publication history to " + historyFile, exception);
        }
    }
}
