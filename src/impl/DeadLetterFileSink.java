package impl;

import core.FailedRecord;
import core.Sink;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DeadLetterFileSink implements Sink<FailedRecord> {
    private final String path;
    private PrintWriter writer;

    public DeadLetterFileSink(String path) {
        this.path = path;
    }

    @Override
    public void open() {
        try {
            writer = new PrintWriter(new FileWriter(path, false));
        } catch (IOException e) {
            throw new RuntimeException("Cannot open dead-letter file: " + path, e);
        }
    }

    @Override
    public void consume(FailedRecord record) {
        writer.println(record.toString());
    }

    @Override
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}