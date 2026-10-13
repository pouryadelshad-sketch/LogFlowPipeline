import core.LogRecord;
import impl.PredicateFilterStage;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PredicateFilterStageTest {
    @Test
    void testFilterKeepsMatchingRecords() throws Exception {
        PredicateFilterStage stage = new PredicateFilterStage(r -> r.status() >= 400);
        CollectingEmitter<LogRecord> emitter = new CollectingEmitter<>();

        LogRecord errorRecord = new LogRecord(Instant.now(), "127.0.0.1", "GET", "/api", 404, 100, "ua", null, null);
        LogRecord successRecord = new LogRecord(Instant.now(), "127.0.0.1", "GET", "/home", 200, 100, "ua", null, null);

        stage.process(errorRecord, emitter);
        stage.process(successRecord, emitter);

        assertEquals(1, emitter.getItems().size());
        assertEquals(404, emitter.getItems().get(0).status());
    }
}