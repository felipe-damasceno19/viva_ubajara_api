package io.github.parqueubajara.api.exception;

public class DuplicateRegistry extends RuntimeException {
    public DuplicateRegistry(String message) {
        super(message);
    }
}
