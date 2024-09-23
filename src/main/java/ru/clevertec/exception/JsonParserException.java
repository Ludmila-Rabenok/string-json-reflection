package ru.clevertec.exception;

public class JsonParserException extends RuntimeException {

    public JsonParserException(String message) {
        super(message);
    }

    public JsonParserException(Throwable t) {
        super(t);
    }

    public JsonParserException() {
        super();
    }
}
