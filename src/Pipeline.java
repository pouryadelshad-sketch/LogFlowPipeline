import core.Emitter;
import core.Sink;
import core.Source;
import core.Stage;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Pipeline {
    private final Source source;
    private final List<Stage> stages;
    private final Sink sink;

    public Pipeline(Source source, List<Stage> stages, Sink sink) {
        this.source = source;
        this.stages = stages;
        this.sink = sink;
    }

    public void run() {
        // 1. Open all components
        for (Stage stage : stages) stage.open();
        sink.open();

        // 2. Wire the pipeline from back to front (right to left)
        Emitter currentEmitter = sink::consume;
        for (int i = stages.size() - 1; i >= 0; i--) {
            Stage stage = stages.get(i);
            Emitter next = currentEmitter;
            currentEmitter = item -> {
                try {
                    stage.process(item, next);
                } catch (Exception e) {
                    System.err.println("Pipeline process error: " + e.getMessage());
                }
            };
        }

        // 3. Start pulling data from the source
        source.produce(currentEmitter);

        // 4. Close all components (Critical for FileSink to save data)
        for (Stage stage : stages) stage.close();
        sink.close();
    }
}