import core.LogRecord;
import impl.ProjectionStage;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectionStageTest {
    @Test
    void testProjectionRemovesUnselectedFields() throws Exception {
        ProjectionStage stage = new ProjectionStage(List.of("clientIp", "status"));
        CollectingEmitter<LogRecord> emitter = new CollectingEmitter<>();

        LogRecord record = new LogRecord(Instant.now(), "192.168.1.1", "POST", "/data", 201, 5000, "Mozilla", null, null);
        stage.process(record, emitter);

        LogRecord projected = emitter.getItems().get(0);
        assertEquals("192.168.1.1", projected.clientIp());
        assertEquals(201, projected.status());
        assertNull(projected.method());
        assertNull(projected.path());
        assertNull(projected.timestamp());
    }
}