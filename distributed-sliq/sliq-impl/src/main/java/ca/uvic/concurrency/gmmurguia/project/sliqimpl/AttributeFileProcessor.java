package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This will do most of the heavy-lifting for the SLIQ algorithm. Depending on the attribute it will handle the
 * underlying data structures and maintain the state of the tree.
 */
public interface AttributeFileProcessor {

    /**
     * If any initialization is needed. It will be called before any actual processing is requested.
     */
    void init();

    /**
     * Adds a row for the associated attribute.
     *
     * @param index the row index.
     * @param value the value associated.
     * @throws IOException if it was unable to add the row to the underlying data structure.
     */
    void addRow(Integer index, String value) throws IOException;

    /**
     * Returns an iterator for the attribute's rows.
     *
     * @return an iterator for the attribute's rows.
     * @throws IOException if the underlying data structure was unable to obtain the iterator.
     */
    Iterator<String[]> getIterator() throws IOException;

    /**
     * Sorts the rows of this attribute.
     */
    void sortAttribute();

    /**
     * Closes any resources needed by the processor. It will be called when the processor is not longer in use,
     * just before being discarded.
     */
    void close();

    /**
     * Returns <code>true</code> if this attribute is a class attribute.
     *
     * @return <code>true</code> if this attribute is a class attribute.
     */
    boolean isClassAttribute();

    /**
     * Returns the name of the attribute.
     * @return the name of the attribute.
     */
    String getAttributeName();

    /**
     * Follows the Null Object pattern, to avoid NPEs
     */
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

    /**
     * Returns the entropy processor for this attribute.
     *
     * @return the entropy processor for this attribute.
     */
    EntropyProcessor getEntropyProcessor();

    /**
     * Processes the attribute, i.e. creates class counters for each side and invokes its entropy processor.
     *
     * @param classCounters the global class counters.
     * @param processedValues the global processed values.
     */
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
            // Not really interested in handling, so just propagate all the way
            throw new RuntimeException(e);
        }
    }
}
