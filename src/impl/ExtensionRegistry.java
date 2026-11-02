package impl;

import core.ErrorPolicy;
import core.LogRecord;
import core.StageRegistry;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class ExtensionRegistry {

    public static void registerAll() {
        // 1. Parser Stage (Reads errorPolicy from YAML)
        StageRegistry.register("parse", params -> {
            String policyStr = (String) params.getOrDefault("errorPolicy", "FAIL_FAST");
            return new ParserStage(ErrorPolicy.valueOf(policyStr.toUpperCase()));
        });

        // 2. Filter Stage
        StageRegistry.register("filter", params -> {
            if (!params.containsKey("field") || !params.containsKey("op") || !params.containsKey("value")) {
                throw new IllegalArgumentException("Configuration Error: 'filter' stage requires 'field', 'op', and 'value'.");
            }

            String field = (String) params.get("field");
            String op = (String) params.get("op");
            String valueStr = String.valueOf(params.get("value"));

            if (field.equals("status")) {
                try {
                    Integer.parseInt(valueStr);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Configuration Error: 'value' must be a number for filter stage.");
                }
            }

            Predicate<LogRecord> condition = r -> {
                if (field.equals("status") && op.equals(">=")) return r.status() >= Integer.parseInt(valueStr);
                if (field.equals("path") && op.equals("startsWith")) return r.path() != null && r.path().startsWith(valueStr);
                return true;
            };
            return new PredicateFilterStage(condition);
        });

        // 3. Projection Stage
        StageRegistry.register("project", params -> {
            if (!params.containsKey("fields")) throw new IllegalArgumentException("Configuration Error: 'project' stage requires a 'fields' list.");
            Object fieldsObj = params.get("fields");
            if (!(fieldsObj instanceof List)) throw new IllegalArgumentException("Configuration Error: 'fields' must be a YAML list (e.g., [timestamp, status]).");
            return new ProjectionStage((List<String>) fieldsObj);
        });

        // 4. Validation Stage (Reads errorPolicy from YAML)
        StageRegistry.register("validate", params -> {
            boolean rejectNegativeBytes = (boolean) params.getOrDefault("rejectNegativeBytes", true);
            boolean validateTimestamp = (boolean) params.getOrDefault("validateTimestampRange", true);
            int minYear = (int) params.getOrDefault("minYear", 2000);
            int maxYear = (int) params.getOrDefault("maxYear", 2030);
            String policyStr = (String) params.getOrDefault("errorPolicy", "FAIL_FAST");

            return new ValidationStage(rejectNegativeBytes, validateTimestamp, minYear, maxYear, ErrorPolicy.valueOf(policyStr.toUpperCase()));
        });
    }
}