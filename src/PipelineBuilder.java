import core.Sink;
import core.Source;
import core.Stage;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PipelineBuilder {
    private Source source;
    private final List<Stage> stages = new ArrayList<>();

    public static PipelineBuilder from(Source source) {
        PipelineBuilder builder = new PipelineBuilder();
        builder.source = source;
        return builder;
    }

    public PipelineBuilder via(Stage stage) {
        stages.add(stage);
        return this;
    }

    public Pipeline to(Sink sink) {
        return new Pipeline(source, stages, sink);
    }
}