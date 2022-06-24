package me.fero.ascent.exceptions;

public class LimitReachedException extends RuntimeException {
    private final int size;

    public LimitReachedException(String message, int size) {
        super(message);

        this.size = size;
    }

    public int getSize() {
        return size;
    }
}