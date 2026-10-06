import core.Sink;
import core.Source;
import core.Stage;
import core.StageException;

public class Pipeline<I, O> {
    private final Source<I> source;
    private final Stage<I, O> stage;
    private final Sink<O> sink;

    public Pipeline(Source<I> source, Stage<I, O> stage, Sink<O> sink) {
        this.source = source;
        this.stage = stage;
        this.sink = sink;
    }

    public void run() {
        stage.open();
        source.produce(item -> {
            try {
                stage.process(item, sink::consume);
            } catch (StageException e) {
                System.err.println("Pipeline processing error: " + e.getMessage());
            }
        });
        stage.close();
    }
}