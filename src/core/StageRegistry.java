package core;

import java.util.HashMap;
import java.util.Map;

public class StageRegistry {

    public interface StageFactory {
        Stage create(Map<String, Object> params);
    }

    private static final Map<String, StageFactory> registry = new HashMap<>();

    public static void register(String name, StageFactory factory) {
        registry.put(name, factory);
    }

    public static StageFactory get(String name) {
        if (!registry.containsKey(name)) {
            throw new IllegalArgumentException("Configuration Error: Unknown stage type '" + name + "'");
        }
        return registry.get(name);
    }
}