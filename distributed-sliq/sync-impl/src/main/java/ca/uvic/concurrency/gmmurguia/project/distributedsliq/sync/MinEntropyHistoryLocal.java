package ca.uvic.concurrency.gmmurguia.project.distributedsliq.sync;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyData;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyHistory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;

public class MinEntropyHistoryLocal implements MinEntropyHistory {

    private HashMap<Integer, BigDecimal> minEntropies = new HashMap<>();
    private HashMap<Integer, String[]> minAttrVals = new HashMap<>();
    private HashMap<Integer, String> minProcessors = new HashMap<>();

    @Override
    public void clear() {
        minAttrVals.clear();
        minEntropies.clear();
        minProcessors.clear();
    }

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

    @Override
    public void updateMinEntropiesFor(Integer leaf, MinEntropyData minEntropyData) {
        minEntropies.put(leaf, minEntropyData.getMinEntropy());
        minAttrVals.put(leaf, minEntropyData.getMinAttrVals());
        minProcessors.put(leaf, minEntropyData.getMinProcessor());
    }

    @Override
    public Collection<Integer> getMinEntropiesLeaves() {
        return minEntropies.keySet();
    }

    public String getMinProcessorName(Integer leaf) {
        return minProcessors.get(leaf);
    }

    public String[] getMinAttrVals(Integer leaf) {
        return minAttrVals.get(leaf);
    }
}
