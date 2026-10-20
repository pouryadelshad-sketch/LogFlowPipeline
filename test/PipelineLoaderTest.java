
import impl.ExtensionRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineLoaderTest {

    @BeforeAll
    static void setupRegistry() {
        ExtensionRegistry.registerAll(); // Ensure registry is populated before tests run
    }

    private String createTempYaml(String content) throws Exception {
        File file = File.createTempFile("test-config", ".yaml");
        file.deleteOnExit();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }
        return file.getAbsolutePath();
    }

    @Test
    void testValidConfiguration() throws Exception {
        String yaml = "pipeline:\n" +
                "  name: test-pipe\n" +
                "  source:\n" +
                "    type: file\n" +
                "    params: { path: dummy.log }\n" +
                "  stages:\n" +
                "    - type: parse\n" +
                "      params: {}\n" +
                "  sink:\n" +
                "    type: file\n" +
                "    params: { path: out.log }";

        String path = createTempYaml(yaml);
        Pipeline pipeline = PipelineLoader.load(path);
        assertNotNull(pipeline, "Pipeline should load successfully");
    }

    @Test
    void testUnknownStageType() throws Exception {
        String yaml = "pipeline:\n" +
                "  source: { type: file, params: { path: in.log } }\n" +
                "  stages: [ { type: magic, params: {} } ]\n" +
                "  sink: { type: file, params: { path: out.log } }";

        String path = createTempYaml(yaml);
        Exception e = assertThrows(RuntimeException.class, () -> PipelineLoader.load(path));
        assertTrue(e.getMessage().contains("Unknown stage type 'magic'"));
    }

    @Test
    void testMissingParameter() throws Exception {
        String yaml = "pipeline:\n" +
                "  source: { type: file, params: { path: in.log } }\n" +
                "  stages: [ { type: filter, params: { field: status } } ]\n" + // Missing op and value
                "  sink: { type: file, params: { path: out.log } }";

        String path = createTempYaml(yaml);
        Exception e = assertThrows(RuntimeException.class, () -> PipelineLoader.load(path));
        assertTrue(e.getMessage().contains("requires 'field', 'op', and 'value'"));
    }

    @Test
    void testWrongTypeParameter() throws Exception {
        String yaml = "pipeline:\n" +
                "  source: { type: file, params: { path: in.log } }\n" +
                "  stages: [ { type: filter, params: { field: status, op: '>=', value: 'not-a-number' } } ]\n" +
                "  sink: { type: file, params: { path: out.log } }";

        String path = createTempYaml(yaml);
        Exception e = assertThrows(RuntimeException.class, () -> PipelineLoader.load(path));
        assertTrue(e.getMessage().contains("must be a number"));
    }

    @Test
    void testEmptyPipeline() throws Exception {
        String yaml = "pipeline:\n" +
                "  source: { type: file, params: { path: in.log } }\n" +
                "  sink: { type: file, params: { path: out.log } }"; // No stages list at all

        String path = createTempYaml(yaml);
        Exception e = assertThrows(RuntimeException.class, () -> PipelineLoader.load(path));
        assertTrue(e.getMessage().contains("stages' list is empty or missing"));
    }
}