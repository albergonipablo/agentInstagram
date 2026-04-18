package com.clanofcrows.instagram.domain;

public enum PublicationType {
    STORY("stories"),
    FEED_POST("posts");

    private final String pathSegment;

    PublicationType(String pathSegment) {
        this.pathSegment = pathSegment;
    }

    public String pathSegment() {
        return pathSegment;
    }
}
