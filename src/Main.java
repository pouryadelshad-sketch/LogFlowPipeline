import core.LogRecord;
import impl.FileLineSource;
import impl.FileSink;
import impl.ParserStage;
import impl.PredicateFilterStage;
import impl.ProjectionStage;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Main {
    public static void main(String[] args) {
        // We now need at least an input and output file
        if (args.length < 2) {
            System.err.println("Usage: logflow <input-file> <output-file> [--min-status <status>] [--path-prefix <prefix>] [--fields <f1,f2>]");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        int minStatus = 0;
        String pathPrefix = null;
        List<String> fields = null;

        // Parse command line arguments
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "--min-status":
                    minStatus = Integer.parseInt(args[++i]);
                    break;
                case "--path-prefix":
                    pathPrefix = args[++i];
                    break;
                case "--fields":
                    fields = Arrays.asList(args[++i].split(","));
                    break;
            }
        }

        // Initialize base components
        FileLineSource source = new FileLineSource(inputFile);
        ParserStage parser = new ParserStage();
        FileSink sink = new FileSink(outputFile);

        // Start building the pipeline fluently
        PipelineBuilder builder = PipelineBuilder.from(source).via(parser);

        // Add filter stage if requested
        if (minStatus > 0 || pathPrefix != null) {
            final int finalMinStatus = minStatus;
            final String finalPathPrefix = pathPrefix;

            Predicate<LogRecord> condition = r -> {
                boolean match = true;
                if (finalMinStatus > 0) match = match && r.status() >= finalMinStatus;
                if (finalPathPrefix != null) match = match && r.path() != null && r.path().startsWith(finalPathPrefix);
                return match;
            };
            builder.via(new PredicateFilterStage(condition));
        }

        // Add projection stage if requested
        if (fields != null) {
            builder.via(new ProjectionStage(fields));
        }

        // Finalize and run
        Pipeline pipeline = builder.to(sink);
        pipeline.run();

        System.out.println("Pipeline completed successfully.");
        System.out.println("Total malformed lines skipped: " + parser.getMalformedCount());
    }
}