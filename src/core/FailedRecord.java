package core;

import java.time.Instant;

public class FailedRecord {
    private final String rawData;
    private final String stageName;
    private final Exception exception;
    private final Instant timestamp;

    public FailedRecord(String rawData, String stageName, Exception exception) {
        this.rawData = rawData;
        this.stageName = stageName;
        this.exception = exception;
        this.timestamp = Instant.now();
    }

    public String getRawData() { return rawData; }
    public String getStageName() { return stageName; }
    public Exception getException() { return exception; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] FAILED at '%s': %s | RAW: %s",
                timestamp.toString(), stageName, exception.getMessage(), rawData);
    }
}