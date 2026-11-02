package impl;

import core.PipelineException;
import core.Source;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileLineSource implements Source<String> {
    private final String path;
    private BufferedReader reader;
    private String nextLine;

    public FileLineSource(String path) {
        this.path = path;
    }

    @Override
    public void open() {
        try {
            reader = new BufferedReader(new FileReader(path));
            advance();
        } catch (IOException e) {
            throw new PipelineException("Cannot open source file: " + path, e);
        }
    }

    private void advance() {
        try {
            if (reader != null) {
                nextLine = reader.readLine();
            }
        } catch (IOException e) {
            throw new PipelineException("Error reading file: " + path, e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextLine != null;
    }

    @Override
    public String next() {
        String current = nextLine;
        advance();
        return current;
    }

    @Override
    public void close() {
        try {
            if (reader != null) reader.close();
        } catch (IOException ignore) {}
    }
}