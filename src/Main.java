import core.LogRecord;
import impl.ConsoleSink;
import impl.FileLineSource;
import impl.ParserStage;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: logflow <file-path>");
            System.exit(1);
        }
        String filePath = args[0];

        // 1. Initialize components
        FileLineSource source = new FileLineSource(filePath);
        ParserStage parser = new ParserStage();
        ConsoleSink sink = new ConsoleSink();

        // 2. Assemble pipeline
        Pipeline<String, LogRecord> pipeline = new Pipeline<>(source, parser, sink);

        // 3. Run
        pipeline.run();

        // 4. Report malformed lines total
        System.out.printf("%nTotal malformed lines skipped: %d%n", parser.getMalformedCount());
    }
}