package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface CompleteProcessor {

    void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                 HashMap<String, List<String>> processedValues) throws IOException;


}
