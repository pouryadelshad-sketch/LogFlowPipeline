package core;

// Used when a single record fails, but the pipeline can keep running.
public class StageException extends RuntimeException {
    private final String stageName;
    private final String rawData;

    public StageException(String message, String stageName, String rawData, Throwable cause) {
        super(message, cause);
        this.stageName = stageName;
        this.rawData = rawData;
    }

    public String getStageName() { return stageName; }
    public String getRawData() { return rawData; }
}