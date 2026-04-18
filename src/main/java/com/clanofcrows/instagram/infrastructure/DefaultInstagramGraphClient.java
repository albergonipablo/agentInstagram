package com.clanofcrows.instagram.infrastructure;

import com.clanofcrows.instagram.application.port.InstagramGraphClient;
import com.clanofcrows.instagram.config.InstagramProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class DefaultInstagramGraphClient implements InstagramGraphClient {

    private final RestClient restClient;
    private final String apiVersion;

    public DefaultInstagramGraphClient(RestClient.Builder restClientBuilder, InstagramProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.getGraph().getBaseUrl())
                .build();
        this.apiVersion = properties.getGraph().getApiVersion();
    }

    @Override
    public String createMediaContainer(CreateMediaContainerRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("image_url", request.imageUrl());
        body.add("access_token", request.accessToken());
        if (request.caption() != null && !request.caption().isBlank()) {
            body.add("caption", request.caption());
        }
        if (request.mediaType() != null && !request.mediaType().isBlank()) {
            body.add("media_type", request.mediaType());
        }

        try {
            ContainerCreationResponse response = restClient.post()
                    .uri("/{version}/{igUserId}/media", apiVersion, request.igUserId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(ContainerCreationResponse.class);
            return Objects.requireNonNull(response, "Container creation response must not be null").id();
        } catch (RestClientResponseException exception) {
            log.error("Meta create container request failed with status {} and body {}",
                    exception.getStatusCode(), exception.getResponseBodyAsString());
            throw exception;
        }
    }

    @Override
    public MediaContainerStatus getMediaContainerStatus(String creationId, String accessToken) {
        try {
            ContainerStatusResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{version}/{creationId}")
                            .queryParam("fields", "status_code,status")
                            .queryParam("access_token", accessToken)
                            .build(apiVersion, creationId))
                    .retrieve()
                    .body(ContainerStatusResponse.class);
            ContainerStatusResponse statusResponse = Objects.requireNonNull(
                    response,
                    "Container status response must not be null"
            );
            return new MediaContainerStatus(statusResponse.statusCode(), statusResponse.status());
        } catch (RestClientResponseException exception) {
            log.error("Meta status polling failed with status {} and body {}",
                    exception.getStatusCode(), exception.getResponseBodyAsString());
            throw exception;
        }
    }

    @Override
    public String publishMediaContainer(String igUserId, String creationId, String accessToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("creation_id", creationId);
        body.add("access_token", accessToken);

        try {
            MediaPublishResponse response = restClient.post()
                    .uri("/{version}/{igUserId}/media_publish", apiVersion, igUserId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(MediaPublishResponse.class);
            return Objects.requireNonNull(response, "Media publish response must not be null").id();
        } catch (RestClientResponseException exception) {
            log.error("Meta publish request failed with status {} and body {}",
                    exception.getStatusCode(), exception.getResponseBodyAsString());
            throw exception;
        }
    }

    private record ContainerCreationResponse(String id) {
    }

    private record ContainerStatusResponse(@JsonProperty("status_code") String statusCode, String status) {
    }

    private record MediaPublishResponse(String id) {
    }
}
