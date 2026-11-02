import core.ErrorPolicy;
import core.FailedRecord;
import core.PipelineException;
import core.RunSummary;
import core.Sink;
import core.Source;
import core.Stage;
import core.StageException;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Pipeline {
    private final Source source;
    private final List<Stage> stages;
    private final Sink mainSink;
    private final Sink deadLetterSink;
    private final RunSummary summary;

    public Pipeline(Source source, List<Stage> stages, Sink mainSink, Sink deadLetterSink) {
        this.source = source;
        this.stages = stages;
        this.mainSink = mainSink;
        this.deadLetterSink = deadLetterSink;
        this.summary = new RunSummary();
    }

    public void run() {
        summary.startTimer();

        try {
            source.open();
            mainSink.open();
            if (deadLetterSink != null) deadLetterSink.open();

            while (source.hasNext()) {
                Object currentData = source.next();
                summary.incrementIncoming();

                boolean dropped = false;

                for (Stage stage : stages) {
                    Object[] outputHolder = new Object[1];

                    try {
                        stage.process(currentData, out -> outputHolder[0] = out);

                        if (outputHolder[0] == null) {
                            dropped = true;
                            break;
                        }
                        currentData = outputHolder[0];

                    } catch (StageException e) {
                        handleStageError(e, stage);
                        dropped = true;
                        break;
                    } catch (Exception e) {
                        throw new PipelineException("Fatal unexpected error in pipeline", e);
                    }
                }

                if (!dropped) {
                    mainSink.consume(currentData); // Fixed method name
                    summary.incrementOutgoing();
                }
            }
        } finally {
            source.close();
            mainSink.close();
            if (deadLetterSink != null) deadLetterSink.close();

            summary.stopTimer();
            summary.printReport();
        }
    }

    private void handleStageError(StageException e, Stage stage) {
        summary.recordDrop(e.getStageName());
        ErrorPolicy policy = stage.getErrorPolicy();

        if (policy == ErrorPolicy.FAIL_FAST) {
            throw new PipelineException("Pipeline failed fast due to error at stage: " + e.getStageName(), e);
        } else if (policy == ErrorPolicy.DEAD_LETTER) {
            summary.incrementDeadLetter();
            if (deadLetterSink != null) {
                FailedRecord fr = new FailedRecord(e.getRawData(), e.getStageName(), e);
                deadLetterSink.consume(fr); // Fixed method name
            }
        }
    }
}