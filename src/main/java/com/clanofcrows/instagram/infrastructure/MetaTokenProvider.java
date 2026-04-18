package com.clanofcrows.instagram.infrastructure;

import com.clanofcrows.instagram.config.InstagramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetaTokenProvider {

    private final InstagramProperties properties;

    public String accessToken() {
        return properties.getAccessToken();
    }

    public String igUserId() {
        return properties.getIgUserId();
    }
}
