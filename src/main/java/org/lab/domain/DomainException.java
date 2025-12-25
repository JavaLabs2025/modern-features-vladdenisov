package org.lab.domain;

public final class DomainException extends RuntimeException {
    public DomainException(String message) {
        // Flexible Constructor Bodies
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message is blank");
        }
        super(message);
    }
}




