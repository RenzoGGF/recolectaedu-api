package com.recolectaedu.exception;

public class AlmacenamientoException extends RuntimeException {

    public AlmacenamientoException(String message) {
        super(message);
    }

    public AlmacenamientoException(String message, Throwable cause) {
        super(message, cause);
    }
}
