package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface EntropyProcessor {

    void processAttributeEntropies(String attribute, HashMap<Integer, HashMap<String, Integer>> currentClassCountersR,
                                   HashMap<Integer, HashMap<String, Integer>> currentClassCountersL,
                                   List<String> processed) throws IOException;

}
