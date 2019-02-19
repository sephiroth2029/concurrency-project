package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Coordinator for the entropy computation for each division of the dataset.
 */
public interface EntropyProcessor {

    /**
     * Processes the entropy for the specified attribute.
     *
     * @param attribute the target attribute.
     * @param currentClassCountersR the counters on the right.
     * @param currentClassCountersL the counters on the left.
     * @param processed the labels of the attributes processed.
     * @throws IOException in case of an error in the underlying data structures.
     */
    void processAttributeEntropies(String attribute, HashMap<Integer, HashMap<String, Integer>> currentClassCountersR,
                                   HashMap<Integer, HashMap<String, Integer>> currentClassCountersL,
                                   List<String> processed) throws IOException;

}
