package com.clanofcrows.instagram.application;

public class PublicationGatewayException extends RuntimeException {

    private final String containerId;

    public PublicationGatewayException(String message, String containerId, RuntimeException cause) {
        super(message, cause);
        this.containerId = containerId;
    }

    public String containerId() {
        return containerId;
    }

    public RuntimeException toRuntimeException() {
        return getCause() instanceof RuntimeException runtimeException ? runtimeException : this;
    }
}
