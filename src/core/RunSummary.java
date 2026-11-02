package core;

import java.util.LinkedHashMap;
import java.util.Map;

public class RunSummary {
    private int incomingRecords = 0;
    private int outgoingRecords = 0;
    private int deadLetterRecords = 0;
    private long startTime;
    private long endTime;

    // Tracks dropped/skipped records per stage
    private final Map<String, Integer> droppedPerStage = new LinkedHashMap<>();

    public void startTimer() { this.startTime = System.currentTimeMillis(); }
    public void stopTimer() { this.endTime = System.currentTimeMillis(); }

    public void incrementIncoming() { incomingRecords++; }
    public void incrementOutgoing() { outgoingRecords++; }
    public void incrementDeadLetter() { deadLetterRecords++; }

    public void recordDrop(String stageName) {
        droppedPerStage.put(stageName, droppedPerStage.getOrDefault(stageName, 0) + 1);
    }

    public void printReport() {
        System.out.println("\n=== PIPELINE RUN SUMMARY ===");
        System.out.println("Execution Time: " + (endTime - startTime) + " ms");
        System.out.println("Incoming Records: " + incomingRecords);
        System.out.println("Outgoing (Valid) Records: " + outgoingRecords);
        System.out.println("Dead-Letter (Failed) Records: " + deadLetterRecords);
        System.out.println("Dropped Records by Stage:");
        if (droppedPerStage.isEmpty()) {
            System.out.println("  None");
        } else {
            droppedPerStage.forEach((stage, count) ->
                    System.out.println("  - " + stage + ": " + count)
            );
        }
        System.out.println("============================");
    }
}