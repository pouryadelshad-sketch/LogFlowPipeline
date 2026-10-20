package core;

import java.util.HashMap;
import java.util.Map;

public class StageRegistry {
    private static final Map<String, StageFactory> registry = new HashMap<>();

    public static void register(String typeName, StageFactory factory) {
        registry.put(typeName, factory);
    }

    public static Stage createStage(String typeName, Map<String, Object> params) {
        if (!registry.containsKey(typeName)) {
            // Fails fast on unknown stage types
            throw new IllegalArgumentException("Configuration Error: Unknown stage type '" + typeName + "'");
        }
        return registry.get(typeName).create(params);
    }
}