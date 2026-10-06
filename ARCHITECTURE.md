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