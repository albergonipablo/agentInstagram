package com.clanofcrows.instagram.publication;

import com.clanofcrows.instagram.application.PublishFeedPostUseCase;
import com.clanofcrows.instagram.application.PublishStoryUseCase;
import com.clanofcrows.instagram.application.PublicationExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublishFeedPostUseCase publishFeedPostUseCase;
    private final PublishStoryUseCase publishStoryUseCase;

    @PostMapping("/feed-posts")
    public PublicationExecutionResult createFeedPostPublication(@RequestParam(required = false) String fileName) {
        return fileName == null || fileName.isBlank()
                ? publishFeedPostUseCase.publishNext()
                : publishFeedPostUseCase.publish(fileName);
    }

    @PostMapping("/stories")
    public PublicationExecutionResult createStoryPublication(@RequestParam(required = false) String fileName) {
        return fileName == null || fileName.isBlank()
                ? publishStoryUseCase.publishNext()
                : publishStoryUseCase.publish(fileName);
    }
}
