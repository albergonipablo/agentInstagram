package com.clanofcrows.instagram.application.port;

import com.clanofcrows.instagram.domain.MediaAsset;

public interface PublicationGateway {

    PublicationReceipt publish(MediaAsset mediaAsset);

    record PublicationReceipt(String containerId, String publishId) {
    }
}
