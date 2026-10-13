package impl;

import core.LogRecord;
import core.Sink;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileSink implements Sink<LogRecord> {
    private final String outputPath;
    private BufferedWriter writer;

    public FileSink(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void open() {
        try {
            writer = new BufferedWriter(new FileWriter(outputPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not open file: " + outputPath, e);
        }
    }

    @Override
    public void consume(LogRecord item) {
        try {
            writer.write(String.format("[%s] %s %s %s -> HTTP %d (%d bytes)",
                    item.timestamp(), item.clientIp(), item.method(), item.path(), item.status(), item.bytes()));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Write error", e);
        }
    }

    @Override
    public void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Close error", e);
        }
    }
}