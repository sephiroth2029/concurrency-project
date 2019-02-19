package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.CompleteProcessor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The processor to be used asynchronously, i.e. in the distributed primitives without blocking. It didn't quite work,
 * as the consensus overhead brings the system down.
 */
@Getter
@Setter
@AllArgsConstructor
public class CompleteProcessorAsync implements CompleteProcessor {

    private AttributeFileProcessor[] attributeFileProcessors;

    /**
     * Processes all attributes but the class's.
     *
     * @param classCounters the counters. Needed by the processor to keep track of updates.
     * @param processedValues the current values.
     */
    @Override
    public void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                        HashMap<String, List<String>> processedValues) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (AttributeFileProcessor processor : attributeFileProcessors) {
            if (processor.isClassAttribute()) continue;

            futures.add(CompletableFuture.supplyAsync(() -> {
                processor.process(classCounters, processedValues);
                return null;
            }));
        }

        for (CompletableFuture<Void> future : futures) {
            future.join();
        }
    }
}
