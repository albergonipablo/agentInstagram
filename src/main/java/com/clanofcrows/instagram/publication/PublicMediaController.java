package com.clanofcrows.instagram.publication;

import com.clanofcrows.instagram.domain.MediaAsset;
import com.clanofcrows.instagram.domain.PublicationType;
import com.clanofcrows.instagram.infrastructure.LocalMediaRepository;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/media")
@RequiredArgsConstructor
public class PublicMediaController {

    private final LocalMediaRepository localMediaRepository;

    @GetMapping("/{type}/{fileName:.+}")
    public ResponseEntity<Resource> serveMedia(
            @PathVariable String type,
            @PathVariable String fileName
    ) throws IOException {
        PublicationType publicationType = switch (type) {
            case "stories" -> PublicationType.STORY;
            case "posts" -> PublicationType.FEED_POST;
            default -> throw new PublicationRequestException("Unsupported media type path: " + type);
        };

        MediaAsset mediaAsset = localMediaRepository.findByFileName(publicationType, fileName)
                .orElseThrow(() -> new PublicationRequestException("Media file not found: " + fileName));

        String contentType = Files.probeContentType(mediaAsset.path());
        MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(mediaAsset.path()));
    }
}
