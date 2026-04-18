package com.clanofcrows.instagram.publication;

import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PublicationExceptionHandler {

    @ExceptionHandler(PublicationRequestException.class)
    public ProblemDetail handleBadRequest(PublicationRequestException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Invalid publication request");
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        return problemDetail;
    }

    @ExceptionHandler(PublicationExecutionException.class)
    public ProblemDetail handleExecutionFailure(PublicationExecutionException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, exception.getMessage());
        problemDetail.setTitle("Instagram publication failed");
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        return problemDetail;
    }
}
