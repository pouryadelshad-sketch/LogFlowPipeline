package impl;

import core.Emitter;
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

    // Matches: IP, timestamp, method, path, status, bytes, and optional user agent
    // \s+ handles multiple spaces between tokens
    private static final Pattern CLF_PATTERN = Pattern.compile(
            "^(\\S+)\\s+\\S+\\s+\\S+\\s+\\[([^\\]]+)\\]\\s+\"(\\S+)\\s+(\\S+)\\s+[^\"]*\"\\s+(\\S+)\\s+(\\S+)(?:\\s+\"[^\"]*\"\\s+\"([^\"]*)\")?.*$"
    );

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    private int malformedCount = 0;

    @Override
    public void process(String input, Emitter<LogRecord> out) throws StageException {
        if (input == null || input.trim().isEmpty()) {
            malformedCount++;
            return;
        }

        Matcher matcher = CLF_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            malformedCount++;
            return;
        }

        try {
            String ip = matcher.group(1);
            String rawDate = matcher.group(2);
            String method = matcher.group(3);
            String path = matcher.group(4);
            String rawStatus = matcher.group(5);
            String rawBytes = matcher.group(6);
            String userAgent = matcher.group(7) != null ? matcher.group(7) : "-";

            // Parse and validate timestamp
            Instant timestamp = OffsetDateTime.parse(rawDate, DATE_FORMATTER).toInstant();

            // Parse and validate status (must be valid 3-digit HTTP status code)
            int status = Integer.parseInt(rawStatus);
            if (status < 100 || status > 599) {
                malformedCount++;
                return;
            }

            // Parse byte size ("-" indicates 0 bytes in CLF)
            long bytes = "-".equals(rawBytes) ? 0 : Long.parseLong(rawBytes);

            LogRecord record = new LogRecord(
                    timestamp,
                    ip,
                    method,
                    path,
                    status,
                    bytes,
                    userAgent,
                    null,
                    input
            );

            out.emit(record);

        } catch (DateTimeParseException | NumberFormatException e) {
            malformedCount++;
        }
    }

    public int getMalformedCount() {
        return malformedCount;
    }
}