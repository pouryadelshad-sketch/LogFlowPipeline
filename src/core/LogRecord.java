package core;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public record LogRecord(
        Instant timestamp,
        String clientIp,
        String method,
        String path,
        int status,
        long bytes,
        String userAgent,
        Map<String, String> attributes, // Extension point for weeks 5-7
        String raw
) {
    public LogRecord {
        // Ensure attributes cannot be mutated externally
        attributes = attributes == null ? Map.of() : Collections.unmodifiableMap(attributes);
    }
}