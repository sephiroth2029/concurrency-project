package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.CompleteProcessor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

/**
 * The processor to be used asynchronously, i.e. in the distributed primitives without blocking.
 */
@Getter
@Setter
@AllArgsConstructor
public class CompleteProcessorAsync implements CompleteProcessor {

    private AttributeFileProcessor[] attributeFileProcessors;

    /**
     * Processes all attributes but the class one.
     *
     * @param classCounters the counters. Needed by the processor to keep track of updates.
     * @param processedValues the current values.
     */
    @Override
    public void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                        HashMap<String, List<String>> processedValues) {
        for (AttributeFileProcessor processor : attributeFileProcessors) {
            if (processor.isClassAttribute()) continue;

                processor.process(classCounters, processedValues);
        }
    }
}
