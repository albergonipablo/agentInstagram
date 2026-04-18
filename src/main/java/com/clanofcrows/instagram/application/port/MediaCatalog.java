package com.clanofcrows.instagram.application.port;

import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationType;
import java.util.List;
import java.util.Optional;

public interface MediaCatalog {

    List<MediaAsset> listAvailableMedia(PublicationType type);

    Optional<MediaAsset> findByFileName(PublicationType type, String fileName);
}
