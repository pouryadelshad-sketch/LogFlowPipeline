package impl;

import core.LogRecord;
import core.Sink;

public class ConsoleSink implements Sink<LogRecord> {
    @Override
    public void consume(LogRecord record) {
        System.out.printf("[%s] %s %s %s -> HTTP %d (%d bytes)%n",
                record.timestamp(),
                record.clientIp(),
                record.method(),
                record.path(),
                record.status(),
                record.bytes()
        );
    }
}