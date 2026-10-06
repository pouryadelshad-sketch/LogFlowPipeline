# Architecture - Increment 1
+--------------------+              +-----------------+
|   FileLineSource   |  --------->  |   ConsoleSink   |
|      (Source)      |   records    |     (Sink)      |
+--------------------+              +-----------------+
- **FileLineSource**: Reads records line-by-line from a text file.
- **ConsoleSink**: Prints incoming records to standard output.
- **Pipeline**: Connects the source directly to the sink.


The attributes map provides an open extension point without breaking the immutability of the record.
Stages in later weeks can attach custom metadata (e.g., geo-IP locations, user IDs, security flags) that this initial parser knows nothing about.