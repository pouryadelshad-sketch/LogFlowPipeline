# Changelog - Increment 2

## Added
- `core/LogRecord.java`: Immutable domain record with attributes map.
- `impl/ParserStage.java`: CLF parser with malformed line counting.
- `test/CollectingEmitter.java`: In-memory test double emitter.
- `test/ParserStageTest.java`: 8 JUnit 5 test cases.

## Modified
- `impl/ConsoleSink.java`: Updated to print single-line formatted LogRecords.
- `Pipeline.java`: Updated generics to chain Source -> Stage -> Sink.
- `Main.java`: Wired ParserStage and added malformed line summary.