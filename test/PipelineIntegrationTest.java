import impl.FileLineSource;
import impl.FileSink;
import impl.ParserStage;
import impl.PredicateFilterStage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineIntegrationTest {
    @Test
    void testFullPipelineWithFileIO() throws Exception {
        // 1. Setup temporary input and output files
        File inputFile = File.createTempFile("test-in", ".log");
        File outputFile = File.createTempFile("test-out", ".log");
        inputFile.deleteOnExit();
        outputFile.deleteOnExit();

        // Write 3 records (two 200s, one 404)
        try (FileWriter fw = new FileWriter(inputFile)) {
            fw.write("10.0.0.1 - - [29/Sep/2026:14:00:01 +0000] \"GET / HTTP/1.1\" 200 100\n");
            fw.write("10.0.0.2 - - [29/Sep/2026:14:00:02 +0000] \"GET /err HTTP/1.1\" 404 100\n");
            fw.write("10.0.0.3 - - [29/Sep/2026:14:00:03 +0000] \"GET /api HTTP/1.1\" 200 100\n");
        }

        // 2. Build Pipeline
        Pipeline pipeline = PipelineBuilder.from(new FileLineSource(inputFile.getAbsolutePath()))
                .via(new ParserStage())
                .via(new PredicateFilterStage(r -> r.status() >= 400))
                .to(new FileSink(outputFile.getAbsolutePath()));

        // 3. Run Pipeline
        pipeline.run();

        // 4. Assert Results (Should only contain the 404 line)
        List<String> outputLines = Files.readAllLines(outputFile.toPath());
        assertEquals(1, outputLines.size(), "Only one record should pass the filter");
        assertTrue(outputLines.get(0).contains("HTTP 404"));
    }
}