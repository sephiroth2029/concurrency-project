package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Keeps track of the minimum entropy computation values.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MinEntropyData {

    private BigDecimal minEntropy;
    private String[] minAttrVals;
    private String minProcessor;

}
