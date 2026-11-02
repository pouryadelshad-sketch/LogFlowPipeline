import core.FailedRecord;
import core.Sink;
import core.Source;
import core.Stage;
import core.StageRegistry;
import impl.DeadLetterFileSink;
import impl.FileLineSource;
import impl.FileSink;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PipelineLoader {

    public static Pipeline load(String configPath) throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(configPath)) {
            Map<String, Object> config = yaml.load(in);
            Map<String, Object> pipelineConfig = (Map<String, Object>) config.get("pipeline");

            // 1. Load Source
            Map<String, Object> sourceConfig = (Map<String, Object>) pipelineConfig.get("source");
            Map<String, Object> sourceParams = (Map<String, Object>) sourceConfig.get("params");
            Source source = new FileLineSource((String) sourceParams.get("path"));

            PipelineBuilder builder = PipelineBuilder.from(source);

            // 2. Load Stages (Using raw List to prevent IntelliJ casting errors)
            Object rawStages = pipelineConfig.get("stages");
            if (rawStages == null) {
                throw new IllegalArgumentException("Configuration Error: Pipeline 'stages' list is missing.");
            }

            List rawList = (List) rawStages;
            if (rawList.isEmpty()) {
                throw new IllegalArgumentException("Configuration Error: Pipeline 'stages' list is empty.");
            }

            for (Object item : rawList) {
                Map<String, Object> stageConfig = (Map<String, Object>) item;
                String type = (String) stageConfig.get("type");

                Map<String, Object> params = (Map<String, Object>) stageConfig.get("params");
                if (params == null) {
                    params = Collections.emptyMap();
                }

                Stage stage = StageRegistry.get(type).create(params);
                builder.via(stage);
            }

            // 3. Load Main Sink (Raw type to fix generic mismatch)
            Map<String, Object> sinkConfig = (Map<String, Object>) pipelineConfig.get("sink");
            Map<String, Object> sinkParams = (Map<String, Object>) sinkConfig.get("params");
            Sink mainSink = new FileSink((String) sinkParams.get("path"));

            // 4. Load Dead-Letter Sink
            if (pipelineConfig.containsKey("dead_letter_sink")) {
                Map<String, Object> dlConfig = (Map<String, Object>) pipelineConfig.get("dead_letter_sink");
                Map<String, Object> dlParams = (Map<String, Object>) dlConfig.get("params");
                Sink dlSink = new DeadLetterFileSink((String) dlParams.get("path"));
                builder.withDeadLetterSink(dlSink);
            }

            return builder.to(mainSink);
        }
    }
}