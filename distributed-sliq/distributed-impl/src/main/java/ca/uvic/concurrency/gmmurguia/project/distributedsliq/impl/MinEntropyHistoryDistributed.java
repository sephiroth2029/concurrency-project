package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyData;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyHistory;
import io.atomix.core.Atomix;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Implements the minimum entropy calculation over the distributed primitives.
 */
public class MinEntropyHistoryDistributed implements MinEntropyHistory {

    /**
     * The key associated with the entropies in the distributed primitive.
     */
    public static final String MIN_ENTROPIES = "minEntropies";

    /**
     * The key associated with the values in the distributed primitive.
     */
    public static final String MIN_ATTR_VALS = "minAttrVals";

    /**
     * The key associated with the processors in the distributed primitive.
     */
    public static final String MIN_PROCESSORS = "minProcessors";
    private static Atomix atomix;

    /**
     * Meant to be called right after the object is constructed and either create a new Atomix instance or copy that
     * of the <code>AttributeFileProcessorAtomix</code>.
     */
    @PostConstruct
    public void init() {
        atomix = AttributeFileProcessorAtomix.atomix;
    }

    private Map<Integer, BigDecimal> getMinEntropies() {
        Map<Integer, BigDecimal> minEntropies = atomix.getMap(MIN_ENTROPIES);
        return minEntropies;
    }

    private Map<Integer, String[]> getMinAttrVals() {
        Map<Integer, String[]> minAttrVals = atomix.getMap(MIN_ATTR_VALS);
        return minAttrVals;
    }

    private Map<Integer, String> getMinProcessors() {
        Map<Integer, String> minProcessors = atomix.getMap(MIN_PROCESSORS);
        return minProcessors;
    }

    /**
     * Empties the distributed primitives for the first or a new test.
     */
    @Override
    public void clear() {
        if (atomix == null) {
            atomix = AttributeFileProcessorAtomix.atomix;
        }
        atomix.getMap(MIN_ATTR_VALS).clear();
        atomix.getMap(MIN_ENTROPIES).clear();
        atomix.getMap(MIN_PROCESSORS).clear();
    }

    /**
     * Obtains the minimum entropy for the corresponding leaf.
     *
     * @param leaf the target leaf.
     * @return the minimum entropy for the corresponding leaf.
     */
    @Override
    public BigDecimal getMinEntropy(Integer leaf) {
        BigDecimal minEntropy;
        Map<Integer, BigDecimal> minEntropies = getMinEntropies();
        if (!minEntropies.containsKey(leaf)) {
            minEntropy = BigDecimal.valueOf(Integer.MAX_VALUE);
        } else {
            minEntropy = minEntropies.get(leaf);
        }

        return minEntropy;
    }

    /**
     * Updates the entropies for the given leaf.
     *
     * @param leaf the target leaf.
     * @param minEntropyData the entropies data to be updated.
     */
    @Override
    public void updateMinEntropiesFor(Integer leaf, MinEntropyData minEntropyData) {
        getMinEntropies().put(leaf, minEntropyData.getMinEntropy());
        getMinAttrVals().put(leaf, minEntropyData.getMinAttrVals());
        getMinProcessors().put(leaf, minEntropyData.getMinProcessor());
    }

    /**
     * Obtains the processor name for the given leaf.
     *
     * @param leaf the target leaf.
     * @return the processor name.
     */
    @Override
    public String getMinProcessorName(Integer leaf) {
        return getMinProcessors().get(leaf);
    }

    /**
     * Obtains the leaf value for all the processed entropies.
     *
     * @return
     */
    @Override
    public Collection<Integer> getMinEntropiesLeaves() {
        return getMinEntropies().keySet();
    }

    /**
     * Obtains the values for the minimum entropies.
     *
     * @param leaf the target leaf.
     * @return the values for the minimum entropies.
     */
    @Override
    public String[] getMinAttrVals(Integer leaf) {
        return getMinAttrVals().get(leaf);
    }
}
