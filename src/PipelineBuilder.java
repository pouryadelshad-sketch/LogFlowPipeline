import core.Sink;
import core.Source;
import core.Stage;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes"})
public class PipelineBuilder {
    private final Source source;
    private final List<Stage> stages = new ArrayList<>();
    private Sink deadLetterSink; // The new secondary branch!

    private PipelineBuilder(Source source) {
        this.source = source;
    }

    public static PipelineBuilder from(Source source) {
        return new PipelineBuilder(source);
    }

    public PipelineBuilder via(Stage stage) {
        stages.add(stage);
        return this;
    }

    // New method to attach the dead-letter channel
    public PipelineBuilder withDeadLetterSink(Sink deadLetterSink) {
        this.deadLetterSink = deadLetterSink;
        return this;
    }

    public Pipeline to(Sink mainSink) {
        return new Pipeline(source, stages, mainSink, deadLetterSink);
    }
}