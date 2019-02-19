package ca.uvic.concurrency.gmmurguia.project.distributedsliq.sync;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyData;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyHistory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;

/**
 * Keeps the history of entropies in memory.
 */
public class MinEntropyHistoryLocal implements MinEntropyHistory {

    private HashMap<Integer, BigDecimal> minEntropies = new HashMap<>();
    private HashMap<Integer, String[]> minAttrVals = new HashMap<>();
    private HashMap<Integer, String> minProcessors = new HashMap<>();

    /**
     * Clears the inner mapping.
     */
    @Override
    public void clear() {
        minAttrVals.clear();
        minEntropies.clear();
        minProcessors.clear();
    }

    /**
     *  Obtains the minimum entropy for the given leaf.
     *
     * @param leaf the target leaf.
     * @return
     */
    @Override
    public BigDecimal getMinEntropy(Integer leaf) {
        BigDecimal minEntropy;
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
     * @param leaf           the target leaf.
     * @param minEntropyData the new data.
     */
    @Override
    public void updateMinEntropiesFor(Integer leaf, MinEntropyData minEntropyData) {
        minEntropies.put(leaf, minEntropyData.getMinEntropy());
        minAttrVals.put(leaf, minEntropyData.getMinAttrVals());
        minProcessors.put(leaf, minEntropyData.getMinProcessor());
    }

    /**
     * Obtains the leaves of the minimum entropies.
     *
     * @return the leaves of the minimum entropies.
     */
    @Override
    public Collection<Integer> getMinEntropiesLeaves() {
        return minEntropies.keySet();
    }

    /**
     * Obtains the name of the minimum entropy processors for the given leaf.
     *
     * @param leaf the target leaf.
     * @return the name of the minimum entropy processors for the given leaf.
     */
    public String getMinProcessorName(Integer leaf) {
        return minProcessors.get(leaf);
    }

    /**
     * Obtains the values of the given leaf.
     *
     * @param leaf the target leaf.
     * @return the values of the given leaf.
     */
    public String[] getMinAttrVals(Integer leaf) {
        return minAttrVals.get(leaf);
    }
}
