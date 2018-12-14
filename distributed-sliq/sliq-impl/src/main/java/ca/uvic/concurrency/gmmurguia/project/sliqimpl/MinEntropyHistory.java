package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.math.BigDecimal;
import java.util.Collection;

public interface MinEntropyHistory {
    void clear();

    BigDecimal getMinEntropy(Integer leaf);

    void updateMinEntropiesFor(Integer leaf, MinEntropyData minEntropyData);

    Collection<Integer> getMinEntropiesLeaves();

    String getMinProcessorName(Integer leaf);

    String[] getMinAttrVals(Integer leaf);
}
