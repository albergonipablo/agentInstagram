package com.clanofcrows.instagram.application.port;

import com.clanofcrows.instagram.domain.PublicationHistory;
import com.clanofcrows.instagram.domain.PublicationHistoryEntry;

public interface PublicationHistoryRepository {

    PublicationHistory getHistory();

    void append(PublicationHistoryEntry entry);
}
