package impl;

import core.Emitter;
import core.LogRecord;
import core.Stage;
import java.util.function.Predicate;

public class PredicateFilterStage implements Stage<LogRecord, LogRecord> {
    private final Predicate<LogRecord> predicate;

    public PredicateFilterStage(Predicate<LogRecord> predicate) {
        this.predicate = predicate;
    }

    @Override
    public void process(LogRecord input, Emitter<LogRecord> out) {
        if (predicate.test(input)) {
            out.emit(input);
        }
    }
}