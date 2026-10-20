# LogFlow Pipeline - Increment 1
## How to Run
1. Open in IntelliJ IDEA.
2. Run `Main.java` with the program argument: `data/access-small.log`.


# LogFlow Pipeline - Increment 2
## How to Run
1. Open in IntelliJ IDEA.
2. Run `Main.java` with argument: `data/access-small.log`.

## Test Coverage
- `ParserStage` Line Coverage: ~96% (8/8 unit tests passing).

## Fail-Fast Configuration Errors
The system validates pipeline configuration strictly at startup before any files are processed. Examples of configuration-time error messages include:

* **Unknown Stage:** `Configuration Error: Unknown stage type 'magic'`
* **Missing Field Parameter:** `Configuration Error: 'project' stage requires a 'fields' list.`
* **Wrong Data Type:** `Configuration Error: 'value' must be a number for filter stage.`
* **Missing Architecture:** `Configuration Error: Pipeline 'stages' list is empty or missing.`