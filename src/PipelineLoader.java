import core.Sink;
import core.Source;
import core.Stage;
import core.StageRegistry;
import impl.FileLineSource;
import impl.FileSink;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PipelineLoader {

    public static Pipeline load(String configPath) {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(configPath)) {
            // Read YAML file
            Map<String, Object> root = yaml.load(in);
            Map<String, Object> pipelineDef = (Map<String, Object>) root.get("pipeline");

            if (pipelineDef == null) {
                throw new IllegalArgumentException("Configuration Error: Missing 'pipeline' root node.");
            }

            // 1. Create Source
            Map<String, Object> sourceDef = (Map<String, Object>) pipelineDef.get("source");
            Map<String, Object> sourceParams = (Map<String, Object>) sourceDef.get("params");
            if (!sourceDef.containsKey("params") || !sourceParams.containsKey("path")) {
                throw new IllegalArgumentException("Configuration Error: Source missing 'path' parameter.");
            }
            Source source = new FileLineSource((String) sourceParams.get("path"));

            // 2. Start Builder
            PipelineBuilder builder = PipelineBuilder.from(source);

            // 3. Create and attach Stages
            List<Map<String, Object>> stagesDef = (List<Map<String, Object>>) pipelineDef.get("stages");
            if (stagesDef == null || stagesDef.isEmpty()) {
                throw new IllegalArgumentException("Configuration Error: Pipeline 'stages' list is empty or missing.");
            }

            for (Map<String, Object> stageDef : stagesDef) {
                String type = (String) stageDef.get("type");
                Map<String, Object> params = (Map<String, Object>) stageDef.get("params");
                if (params == null) params = java.util.Collections.emptyMap();

                // This uses the Registry we built to dynamically build the stage!
                Stage stage = StageRegistry.createStage(type, params);
                builder.via(stage);
            }

            // 4. Create Sink and finalize Pipeline
            Map<String, Object> sinkDef = (Map<String, Object>) pipelineDef.get("sink");
            Map<String, Object> sinkParams = (Map<String, Object>) sinkDef.get("params");
            if (!sinkDef.containsKey("params") || !sinkParams.containsKey("path")) {
                throw new IllegalArgumentException("Configuration Error: Sink missing 'path' parameter.");
            }
            Sink sink = new FileSink((String) sinkParams.get("path"));

            return builder.to(sink);

        } catch (Exception e) {
            // The assignment requires the app to "fail fast, fail loudly" on configuration errors
            throw new RuntimeException("Failed to load config: " + e.getMessage(), e);
        }
    }
}