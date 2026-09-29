import core.Sink;
import core.Source;

public class Pipeline<T> {
    private final Source<T> source;
    private final Sink<T> sink;

    public Pipeline(Source<T> source, Sink<T> sink) {
        this.source = source;
        this.sink = sink;
    }

    public void run() {
        source.produce(sink::consume);
    }
}