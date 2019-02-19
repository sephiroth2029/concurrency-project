package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Does the processing synchronously.
 */
@Getter
@Setter
@AllArgsConstructor
public class CompleteProcessorSync implements CompleteProcessor {

    private AttributeFileProcessor[] attributeFileProcessors;

    /**
     * Processes each attribute.
     *
     * @param classCounters   the global counters.
     * @param processedValues the values processed so far.
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
