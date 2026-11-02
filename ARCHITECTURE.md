# Architecture - Increment 1
+--------------------+              +-----------------+
|   FileLineSource   |  --------->  |   ConsoleSink   |
|      (Source)      |   records    |     (Sink)      |
+--------------------+              +-----------------+
- **FileLineSource**: Reads records line-by-line from a text file.
- **ConsoleSink**: Prints incoming records to standard output.
- **Pipeline**: Connects the source directly to the sink.

## Increment 2: Typed Domain Records & Parser Filter (v2)

### Architecture Evolution
In Increment 2, the pipeline evolved from transmitting unstructured strings to strongly-typed, immutable domain records (`LogRecord`). A parsing filter stage was inserted between the source and sink without altering `FileLineSource`.

### Three-Box Component Diagram
+------------------+             +-----------------+             +-----------------+
|  FileLineSource  | ----------> |   ParserStage   | ----------> |   ConsoleSink   |
|     (Source)     |  raw String |     (Stage)     |  LogRecord  |     (Sink)      |
+------------------+             +-----------------+             +-----------------+
### Key Architectural Concepts
- **Separation of Concerns**: `FileLineSource` only handles file I/O, `ParserStage` handles parsing and CLF validation, and `ConsoleSink` handles formatted output.
- **Push Model (`Emitter<T>`)**: Allows `ParserStage` to produce 0 or 1 outputs per incoming record without using rigid 1-to-1 return types. Malformed lines are simply filtered out and counted.
- **Domain Record Immutability**: `LogRecord` encapsulates validated request telemetry with an unmodifiable `attributes` map designed as an extension point for subsequent stages.
- **Isolated Unit Testing**: `ParserStage` tests interact solely with in-memory test doubles (`CollectingEmitter`) rather than the filesystem.

- The attributes map provides an open extension point without breaking the immutability of the record.
Stages in later weeks can attach custom metadata (e.g., geo-IP locations, user IDs, security flags) that this initial parser knows nothing about.

## Increment 3: Composability & Pipeline Builder

### The Builder Pattern
Instead of hardcoding a 3-component pipeline, the system now utilizes a `PipelineBuilder` to chain an arbitrary number of stages dynamically. This enables command-line flags to dictate pipeline assembly at runtime.

### Pipeline Stage Ordering Experiment
As requested, we evaluated the architectural trade-offs of stage placement:

**Scenario A: Filter BEFORE Projection (Recommended)**
```text
FileLineSource -> ParserStage -> PredicateFilterStage -> ProjectionStage -> FileSink
Performance: Highly efficient. The filter immediately discards irrelevant records (e.g., keeping only status >= 400). The Projection stage only spends CPU cycles formatting the small fraction of records that survive the filter.

Correctness: The filter has access to all original data fields to make its decision.

## Increment 4: Configuration-Driven Pipeline & Registry

### Configuration Schema (YAML)
The pipeline topology is now defined declaratively using YAML. The schema requires a single `pipeline` root node containing:
- `name`: Identifier for the pipeline job.
- `source`: Type and parameters (e.g., file path).
- `stages`: A list of pipeline stages, defined by their `type` (e.g., `parse`, `filter`) and arbitrary `params`.
- `sink`: Output destination type and parameters.

### Stage Registry (The Plugin Architecture)
We eliminated hardcoded `switch` statements for stage selection. Instead, the system uses a `StageRegistry` mapping string identifiers (like "parse") to functional `StageFactory` implementations. This allows new filters or processors to be registered strictly at runtime in `ExtensionRegistry.java` without modifying the core pipeline code (Open/Closed Principle).

### Configuration Loader
`PipelineLoader` utilizes `SnakeYAML` to deserialize the configuration file into nested Java Maps. It walks the configuration tree, validates mandatory parameters, retrieves the necessary factories from the `StageRegistry`, and constructs a finalized `Pipeline` object via the `PipelineBuilder`.

## Increment 5: Error Management & Dead-Letter Channel

### Error Model Definitions
We treat error routing as a topological pipeline decision, not just a standard try/catch block.
* **StageException**: A recoverable, record-level failure (e.g., parsing a malformed string or a negative byte count). The pipeline catches this, routes the specific `FailedRecord` to the configured error policy, and continues processing the next line.
* **PipelineException**: A fatal, system-level failure (e.g., missing YAML configuration, unreadable input file, disk full). The system cannot recover and must fail fast and loudly, terminating the execution.
### Chaos Test Error Mapping
The following table details how the pipeline handles the specific corruptions introduced in `access-dirty.log`:

| Error Type in `access-dirty.log` | Caught By Stage | Configured Policy | Result |
| :--- | :--- | :--- | :--- |
| Truncated lines / Regex mismatch | `ParserStage` | `DEAD_LETTER` | Written to `dead-letter.txt` |
| Negative response bytes (-45) | `ValidationStage` | `DEAD_LETTER` | Written to `dead-letter.txt` |
| Out-of-bounds timestamp (1970/2099) | `ValidationStage` | `DEAD_LETTER` | Written to `dead-letter.txt` |
| Non-UTF-8 characters (ÿþú) | `ValidationStage` | `DEAD_LETTER` | Written to `dead-letter.txt` |
| 100 KiB massive line | `ParserStage` | `DEAD_LETTER` | Written to `dead-letter.txt` |