package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Keeps track of the entropies calculation, to have a baseline to compare to.
 */
public interface MinEntropyHistory {

    /**
     * Clears the history.
     */
    void clear();

    /**
     * Obtains the minimum entropy for the given leaf.
     *
     * @param leaf the target leaf.
     * @return the minimum entropy for the given leaf.
     */
    BigDecimal getMinEntropy(Integer leaf);

    /**
     * Updates the minimun entropy data for the given leaf.
     *
     * @param leaf the target leaf.
     * @param minEntropyData the new data.
     */
    void updateMinEntropiesFor(Integer leaf, MinEntropyData minEntropyData);

    /**
     * Returns the leaves with the minimum entropies.
     *
     * @return the leaves with the minimum entropies.
     */
    Collection<Integer> getMinEntropiesLeaves();

    /**
     * Returns the min processor name for the given leaf.
     *
     * @param leaf the target leaf.
     * @return the min processor name for the given leaf.
     */
    String getMinProcessorName(Integer leaf);

    /**
     * Returns the minimum attribute values for the given leaf.
     *
     * @param leaf the target leaf.
     * @return the minimum attribute values for the given leaf.
     */
    String[] getMinAttrVals(Integer leaf);
}
