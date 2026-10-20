package impl;

import core.LogRecord;
import core.StageRegistry;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class ExtensionRegistry {

    public static void registerAll() {
        // 1. Parser Stage
        StageRegistry.register("parse", params -> new ParserStage());

        // 2. Filter Stage (with strict config-time validation)
        StageRegistry.register("filter", params -> {
            if (!params.containsKey("field") || !params.containsKey("op") || !params.containsKey("value")) {
                throw new IllegalArgumentException("Configuration Error: 'filter' stage requires 'field', 'op', and 'value'.");
            }

            String field = (String) params.get("field");
            String op = (String) params.get("op");
            int value;

            try {
                value = Integer.parseInt(params.get("value").toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Configuration Error: 'value' must be a number for filter stage.");
            }

            Predicate<LogRecord> condition = r -> {
                if (field.equals("status") && op.equals(">=")) return r.status() >= value;
                if (field.equals("path") && op.equals("startsWith")) return r.path() != null && r.path().startsWith(String.valueOf(params.get("value")));
                return true;
            };
            return new PredicateFilterStage(condition);
        });

        // 3. Projection Stage
        StageRegistry.register("project", params -> {
            if (!params.containsKey("fields")) {
                throw new IllegalArgumentException("Configuration Error: 'project' stage requires a 'fields' list.");
            }

            Object fieldsObj = params.get("fields");
            if (!(fieldsObj instanceof List)) {
                throw new IllegalArgumentException("Configuration Error: 'fields' must be a YAML list (e.g., [timestamp, status]).");
            }

            return new ProjectionStage((List<String>) fieldsObj);
        });
    }
}