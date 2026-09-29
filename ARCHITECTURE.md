# Architecture - Increment 1
+--------------------+              +-----------------+
|   FileLineSource   |  --------->  |   ConsoleSink   |
|      (Source)      |   records    |     (Sink)      |
+--------------------+              +-----------------+
- **FileLineSource**: Reads records line-by-line from a text file.
- **ConsoleSink**: Prints incoming records to standard output.
- **Pipeline**: Connects the source directly to the sink.