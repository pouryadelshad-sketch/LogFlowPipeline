package impl;

import core.Emitter;
import core.ErrorPolicy;
import core.LogRecord;
import core.Stage;
import core.StageException;

import java.time.ZoneOffset;
import java.util.regex.Pattern;

public class ValidationStage implements Stage<LogRecord, LogRecord> {
    private final boolean rejectNegativeBytes;
    private final boolean validateTimestampRange;
    private final int minYear;
    private final int maxYear;
    private final ErrorPolicy errorPolicy; // Added policy

    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    public ValidationStage(boolean rejectNegativeBytes, boolean validateTimestampRange, int minYear, int maxYear, ErrorPolicy errorPolicy) {
        this.rejectNegativeBytes = rejectNegativeBytes;
        this.validateTimestampRange = validateTimestampRange;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.errorPolicy = errorPolicy;
    }

    @Override
    public ErrorPolicy getErrorPolicy() {
        return this.errorPolicy;
    }

    @Override
    public void process(LogRecord record, Emitter<LogRecord> emitter) {
        String raw = record.rawText();

        if (record.clientIp() == null || !IP_PATTERN.matcher(record.clientIp()).matches()) {
            throw new StageException("Invalid or missing IP format", "Validation", raw, null);
        }
        if (rejectNegativeBytes && record.bytes() < 0) {
            throw new StageException("Negative byte count", "Validation", raw, null);
        }
        if (validateTimestampRange && record.timestamp() != null) {
            int year = record.timestamp().atZone(ZoneOffset.UTC).getYear();
            if (year < minYear || year > maxYear) {
                throw new StageException("Timestamp out of configured bounds: " + year, "Validation", raw, null);
            }
        }
        if (record.path() == null || record.path().trim().isEmpty()) {
            throw new StageException("Missing required request path", "Validation", raw, null);
        }
        if (record.userAgent() != null && (record.userAgent().contains("ÿþú") || record.userAgent().contains("\uFFFD"))) {
            throw new StageException("Non-UTF-8 characters detected", "Validation", raw, null);
        }

        emitter.emit(record);
    }
}