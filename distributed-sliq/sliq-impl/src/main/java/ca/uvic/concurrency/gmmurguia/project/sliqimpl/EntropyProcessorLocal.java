package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class EntropyProcessorLocal implements EntropyProcessor {

    private AttributeFileProcessor[] attributeFileProcessors;

    @NonNull
    private ClassList classList;

    @NonNull
    private MinEntropyHistory minEntropyHistory;

    @Override
    public void processAttributeEntropies(@NonNull String attribute,
                                          @NonNull HashMap<Integer, HashMap<String, Integer>> currentClassCountersR,
                                          @NonNull HashMap<Integer, HashMap<String, Integer>> currentClassCountersL,
                                          @NonNull List<String> processed) throws IOException {
        AttributeFileProcessor processor = Arrays.stream(attributeFileProcessors)
                .filter(proc -> attribute.equals(proc.getAttributeName()))
                .findFirst()
                .get();
        Iterator<String[]> itProc = processor.getIterator();
        while (itProc.hasNext()) {
            String[] attrVals = itProc.next();
            if (processed.contains(attrVals[1])) {
                continue;
            }

            ClassAttribute currAttr = classList.getClassAttributeOf(attrVals[0]);

            HashMap<String, Integer> currentLCounter = currentClassCountersL.get(currAttr.getLeaf());
            HashMap<String, Integer> currentRCounter = currentClassCountersR.get(currAttr.getLeaf());

            currentLCounter.put(currAttr.getValue(), currentLCounter.get(currAttr.getValue()) + 1);
            currentRCounter.put(currAttr.getValue(), currentRCounter.get(currAttr.getValue()) - 1);

            BigDecimal totalL = BigDecimal.valueOf(currentLCounter.values().stream().mapToInt(Integer::intValue).sum());
            BigDecimal totalR = BigDecimal.valueOf(currentRCounter.values().stream().mapToInt(Integer::intValue).sum());
            BigDecimal total = totalL.add(totalR);

            BigDecimal entropyL = getEntropy(currentLCounter, totalL);
            BigDecimal entropyR = getEntropy(currentRCounter, totalR);

            BigDecimal entropy = entropyL.divide(total).add(entropyR.divide(total));

            BigDecimal minEntropy = minEntropyHistory.getMinEntropy(currAttr.getLeaf());
            if (minEntropy.compareTo(entropy) > 0) {
                minEntropyHistory.updateMinEntropiesFor(currAttr.getLeaf(),
                        new MinEntropyData(minEntropy, attrVals, processor.getAttributeName()));
            }
        }
    }

    private BigDecimal getEntropy(HashMap<String, Integer> currentCounter, BigDecimal total) {
        if (total.equals(BigDecimal.ZERO)) return BigDecimal.ZERO;
        BigDecimal entropyL = BigDecimal.ZERO;
        for (Integer count : currentCounter.values()) {
            BigDecimal divRes = BigDecimal.valueOf(count).divide(total, RoundingMode.HALF_EVEN);
            entropyL.add(divRes.multiply(SliqImpl.Logarithm.log2(divRes.doubleValue())).negate());
        }
        return entropyL.multiply(total);
    }
}
