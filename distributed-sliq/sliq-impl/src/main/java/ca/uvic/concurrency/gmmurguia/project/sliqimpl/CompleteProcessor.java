package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * This processor coordinates how the attributes will be processed.
 */
public interface CompleteProcessor {

    /**
     * Initiates the actual processing.
     *
     * @param classCounters the global counters.
     * @param processedValues the values processed so far.
     * @throws IOException if there is a problem in the underlying data structures.
     */
    void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                 HashMap<String, List<String>> processedValues) throws IOException;


}
