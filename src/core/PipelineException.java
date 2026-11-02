package core;

// Used when a fatal error occurs (e.g., missing config, disk full) and the system must crash.
public class PipelineException extends RuntimeException {
    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String message, Throwable cause) {
        super(message, cause);
    }
}