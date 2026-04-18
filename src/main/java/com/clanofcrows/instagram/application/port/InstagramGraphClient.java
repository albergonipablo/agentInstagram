package com.clanofcrows.instagram.application.port;

public interface InstagramGraphClient {

    String createMediaContainer(CreateMediaContainerRequest request);

    MediaContainerStatus getMediaContainerStatus(String creationId, String accessToken);

    String publishMediaContainer(String igUserId, String creationId, String accessToken);

    record CreateMediaContainerRequest(
            String igUserId,
            String accessToken,
            String imageUrl,
            String caption,
            String mediaType
    ) {
    }

    record MediaContainerStatus(String statusCode, String status) {

        public boolean isFinished() {
            return "FINISHED".equalsIgnoreCase(statusCode);
        }

        public boolean isFailed() {
            return "ERROR".equalsIgnoreCase(statusCode) || "EXPIRED".equalsIgnoreCase(statusCode);
        }
    }
}
