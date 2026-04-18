package com.clanofcrows.instagram.config;

import com.clanofcrows.instagram.domain.PublicationPolicy;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CoreConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    PublicationPolicy publicationPolicy() {
        return new PublicationPolicy();
    }
}
