import core.LogRecord;
import core.StageException;
import impl.ParserStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserStageTest {

    private ParserStage parser;
    private CollectingEmitter<LogRecord> emitter;

    @BeforeEach
    void setUp() {
        parser = new ParserStage();
        emitter = new CollectingEmitter<>();
    }

    // 1. Valid line
    @Test
    void testValidLine() throws StageException {
        String log = "127.0.0.1 - - [29/Sep/2026:14:00:01 +0000] \"GET /index.html HTTP/1.1\" 200 1024 \"-\" \"curl/7.68.0\"";
        parser.process(log, emitter);

        assertEquals(1, emitter.getItems().size());
        assertEquals(0, parser.getMalformedCount());
        LogRecord record = emitter.getItems().get(0);
        assertEquals("127.0.0.1", record.clientIp());
        assertEquals("GET", record.method());
        assertEquals("/index.html", record.path());
        assertEquals(200, record.status());
        assertEquals(1024L, record.bytes());
    }

    // 2. Missing field
    @Test
    void testMissingField() throws StageException {
        String log = "127.0.0.1 - - [29/Sep/2026:14:00:01 +0000] \"GET\" 200";
        parser.process(log, emitter);

        assertTrue(emitter.getItems().isEmpty());
        assertEquals(1, parser.getMalformedCount());
    }

    // 3. Malformed timestamp
    @Test
    void testMalformedTimestamp() throws StageException {
        String log = "127.0.0.1 - - [99/InvalidMonth/2026:99:99:99 +0000] \"GET /index.html HTTP/1.1\" 200 1024";
        parser.process(log, emitter);

        assertTrue(emitter.getItems().isEmpty());
        assertEquals(1, parser.getMalformedCount());
    }

    // 4. Malformed status code
    @Test
    void testMalformedStatusCode() throws StageException {
        String log = "127.0.0.1 - - [29/Sep/2026:14:00:01 +0000] \"GET /index.html HTTP/1.1\" 999 1024";
        parser.process(log, emitter);

        assertTrue(emitter.getItems().isEmpty());
        assertEquals(1, parser.getMalformedCount());
    }

    // 5. Empty line
    @Test
    void testEmptyLine() throws StageException {
        parser.process("   ", emitter);

        assertTrue(emitter.getItems().isEmpty());
        assertEquals(1, parser.getMalformedCount());
    }

    // 6. Extra whitespace
    @Test
    void testExtraWhitespace() throws StageException {
        String log = "127.0.0.1    -   -   [29/Sep/2026:14:00:01 +0000]    \"GET   /home   HTTP/1.1\"   200   512";
        parser.process(log, emitter);

        assertEquals(1, emitter.getItems().size());
        assertEquals(0, parser.getMalformedCount());
        assertEquals("/home", emitter.getItems().get(0).path());
    }

    // 7. Quoted user agent containing spaces
    @Test
    void testQuotedUserAgentWithSpaces() throws StageException {
        String log = "10.0.0.1 - - [29/Sep/2026:14:00:01 +0000] \"GET /api HTTP/1.1\" 200 100 \"https://example.com\" \"Mozilla/5.0 (Windows NT 10.0; Win64; x64)\"";
        parser.process(log, emitter);

        assertEquals(1, emitter.getItems().size());
        assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64)", emitter.getItems().get(0).userAgent());
    }

    // 8. Line with query string
    @Test
    void testLineWithQueryString() throws StageException {
        String log = "127.0.0.1 - - [29/Sep/2026:14:00:01 +0000] \"GET /search?q=test&page=2 HTTP/1.1\" 200 450";
        parser.process(log, emitter);

        assertEquals(1, emitter.getItems().size());
        assertEquals("/search?q=test&page=2", emitter.getItems().get(0).path());
    }
}