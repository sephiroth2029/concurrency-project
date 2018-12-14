package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public interface AttributeFileProcessor {
    void init();

    void addRow(Integer index, String value) throws IOException;

    Iterator<String[]> getIterator() throws IOException;

    void sortAttribute();

    void close();

    boolean isClassAttribute();

    String getAttributeName();

    class NullAttributeFileProcessor implements AttributeFileProcessor {

        @Override
        public void init() {}

        @Override
        public void close() {}

        @Override
        public void addRow(Integer index, String value) {}

        @Override
        public void sortAttribute() {}

        @Override
        public Iterator<String[]> getIterator() {
            return new Iterator<String[]>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public String[] next() {
                    return new String[0];
                }
            };
        }

        @Override
        public boolean isClassAttribute() {
            return false;
        }

        @Override
        public String getAttributeName() {
            return "";
        }

        @Override
        public EntropyProcessor getEntropyProcessor() {
            return null;
        }

        @Override
        public void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                            HashMap<String, List<String>> processedValues) {}
    }

    EntropyProcessor getEntropyProcessor();

    default void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                         HashMap<String, List<String>> processedValues) {
        HashMap<Integer, HashMap<String, Integer>> currentClassCountersR = new HashMap<>();
        HashMap<Integer, HashMap<String, Integer>> currentClassCountersL = new HashMap<>();

        classCounters.keySet().forEach(key -> {
            currentClassCountersR.put(key, new HashMap<>(classCounters.get(key)));
            currentClassCountersL.put(key, new HashMap<>());
            classCounters.get(key)
                    .keySet()
                    .forEach(id -> currentClassCountersL.get(key).put(id, 0));
        });

        List<String> processed;
        if (!processedValues.containsKey(getAttributeName())) {
            processed = new ArrayList<>();
            processedValues.put(getAttributeName(), processed);
        } else {
            processed = processedValues.get(getAttributeName());
        }

        try {
            getEntropyProcessor().processAttributeEntropies(getAttributeName(), currentClassCountersR,
                    currentClassCountersL, processed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
