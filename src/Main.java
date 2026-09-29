import impl.ConsoleSink;
import impl.FileLineSource;

public class Main {
    public static void main(String[] args) {
        // 1. Read command-line argument
        if (args.length < 1) {
            System.err.println("Usage: logflow <file-path>");
            System.exit(1);
        }
        String filePath = args[0];

        // 2. Assemble the pipeline
        FileLineSource source = new FileLineSource(filePath);
        ConsoleSink<String> sink = new ConsoleSink<>();
        Pipeline<String> pipeline = new Pipeline<>(source, sink);

        // 3. Run
        pipeline.run();
    }
}