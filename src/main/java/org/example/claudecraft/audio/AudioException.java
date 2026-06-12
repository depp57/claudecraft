package org.example.claudecraft.audio;

/** Thrown when the audio device cannot be opened or a sound fails to decode. */
public class AudioException extends RuntimeException {

    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
