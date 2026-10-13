package impl;

import core.Emitter;
import core.LogRecord;
import core.Stage;
import java.util.List;
import java.util.Set;

public class ProjectionStage implements Stage<LogRecord, LogRecord> {
    private final Set<String> fields;

    public ProjectionStage(List<String> fields) {
        this.fields = Set.copyOf(fields);
    }

    @Override
    public void process(LogRecord input, Emitter<LogRecord> out) {
        LogRecord projected = new LogRecord(
                fields.contains("timestamp") ? input.timestamp() : null,
                fields.contains("clientIp") ? input.clientIp() : null,
                fields.contains("method") ? input.method() : null,
                fields.contains("path") ? input.path() : null,
                fields.contains("status") ? input.status() : 0,
                fields.contains("bytes") ? input.bytes() : 0L,
                fields.contains("userAgent") ? input.userAgent() : null,
                input.attributes(),
                null
        );
        out.emit(projected);
    }
}