import impl.ExtensionRegistry;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("--config")) {
            System.err.println("Usage: logflow --config <path-to-yaml>");
            System.exit(1);
        }

        String configPath = args[1];

        // 1. Initialize the Registry (Registers all available stages)
        ExtensionRegistry.registerAll();

        // 2. Load the Pipeline dynamically from the YAML file
        try {
            Pipeline pipeline = PipelineLoader.load(configPath);
            pipeline.run();
            System.out.println("Pipeline completed successfully using config: " + configPath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}