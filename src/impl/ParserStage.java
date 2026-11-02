package impl;

import core.Emitter;
import core.ErrorPolicy;
import core.LogRecord;
import core.Stage;
import core.StageException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserStage implements Stage<String, LogRecord> {

    private static final Pattern CLF_PATTERN = Pattern.compile(
            "^(\\S+)\\s+\\S+\\s+\\S+\\s+\\[([^\\]]+)\\]\\s+\"(\\S+)\\s+(\\S+)\\s+[^\"]*\"\\s+(\\S+)\\s+(\\S+)(?:\\s+\"[^\"]*\"\\s+\"([^\"]*)\")?.*$"
    );

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    private final ErrorPolicy errorPolicy;

    // Default constructor for compatibility
    public ParserStage() {
        this.errorPolicy = ErrorPolicy.FAIL_FAST;
    }

    // New constructor to support YAML error policies
    public ParserStage(ErrorPolicy errorPolicy) {
        this.errorPolicy = errorPolicy;
    }

    @Override
    public ErrorPolicy getErrorPolicy() {
        return this.errorPolicy;
    }

    @Override
    public void process(String input, Emitter<LogRecord> out) {
        // We safely ignore completely empty lines (like at the end of a file)
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        Matcher matcher = CLF_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            // PHASE 5: Throw StageException instead of silently skipping
            throw new StageException("Line does not match Common Log Format", "Parser", input, null);
        }

        try {
            String ip = matcher.group(1);
            String rawDate = matcher.group(2);
            String method = matcher.group(3);
            String path = matcher.group(4);
            String rawStatus = matcher.group(5);
            String rawBytes = matcher.group(6);
            String userAgent = matcher.group(7) != null ? matcher.group(7) : "-";

            Instant timestamp = OffsetDateTime.parse(rawDate, DATE_FORMATTER).toInstant();

            int status = Integer.parseInt(rawStatus);
            if (status < 100 || status > 599) {
                // PHASE 5: Throw StageException on bad data
                throw new StageException("Invalid HTTP status code: " + status, "Parser", input, null);
            }

            long bytes = "-".equals(rawBytes) ? 0 : Long.parseLong(rawBytes);

            LogRecord record = new LogRecord(
                    timestamp, ip, method, path, status, bytes, userAgent, null, input
            );

            out.emit(record);

        } catch (DateTimeParseException | NumberFormatException e) {
            // PHASE 5: Catch format errors and wrap them in a StageException
            throw new StageException("Failed to parse numeric or date fields: " + e.getMessage(), "Parser", input, e);
        }
    }
}