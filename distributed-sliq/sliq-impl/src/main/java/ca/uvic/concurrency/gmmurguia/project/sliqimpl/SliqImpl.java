package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import com.opencsv.CSVReader;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Component
@Getter
@Setter
public class SliqImpl {

    @Value("${sliq.sourcePath}")
    private String sourcePath;

    @Autowired
    private AttributeFileProcessor[] attributeFileProcessors;

    @Autowired
    private MinEntropyHistory minEntropyHistory;

    @Autowired
    private ClassList classList;

    @Autowired
    private AttributeFileProcessor classAttributeProcessor;

    @Autowired
    private CompleteProcessor completeProcessor;

    public void start(int targetNodes) throws IOException {
        splitInFiles(sourcePath);
        classList.fill();
        sortAttributes();

        HashMap<String, AttributeFileProcessor> attributeFileProcessorsByAttr = new HashMap<>();
        Arrays.stream(attributeFileProcessors)
                .forEach(proc -> attributeFileProcessorsByAttr.put(proc.getAttributeName(), proc));
        HashMap<Integer, HashMap<String, Integer>> classCounters = new HashMap<>();


        HashMap<String, List<String>> processedValues = new HashMap<>();

        int baseLeaf = 0;
        for (int i = 0; i < targetNodes; i++) {
            updateCounters(classCounters, classList);
            minEntropyHistory.clear();

            completeProcessor.process(classCounters, processedValues);

            for (Integer leaf : minEntropyHistory.getMinEntropiesLeaves()) {
                String minProcessorName = minEntropyHistory.getMinProcessorName(leaf);
                AttributeFileProcessor minProcessor = attributeFileProcessorsByAttr.get(minProcessorName);
                String[] minAttrVals = minEntropyHistory.getMinAttrVals(leaf);
                baseLeaf = updateLeaves(baseLeaf, minAttrVals, minProcessor, classList);
                processedValues.get(minProcessorName).add(minAttrVals[1]);
                System.out.println("Attr: " + minProcessorName);
                System.out.println("minAttrVals.get(leaf)[1]: " + minAttrVals[1]);
                System.out.println("processedValues: " + processedValues);
                System.out.println();
            }
        }
    }

    private int updateLeaves(int baseLeaf, String[] minAttrVals, AttributeFileProcessor processor,
                             ClassList classList) throws IOException {
        boolean isCategorical = !NumberUtils.isNumber(minAttrVals[1]);

        Iterator<String[]> itProc = processor.getIterator();
        boolean found = false;
        while (itProc.hasNext()) {
            String[] attrVals = itProc.next();
            String rid = attrVals[0];
            ClassAttribute ca = classList.getClassAttributeOf(rid);

            if (found) {
                ca.setLeaf(ca.getLeaf() + 2);
                classList.updateClassAttribute(rid, ca);
                continue;
            }

            if (isCategorical) {
                if (attrVals[1].equals(minAttrVals[1])) {
                    ca.setLeaf(baseLeaf + 1);
                } else {
                    ca.setLeaf(baseLeaf + 2);
                    found = true;
                }
            } else {
                if (new BigDecimal(attrVals[1]).compareTo(new BigDecimal(minAttrVals[1])) < 0) {
                    ca.setLeaf(baseLeaf + 1);
                } else {
                    ca.setLeaf(baseLeaf + 2);
                    found = true;
                }
            }
            classList.updateClassAttribute(rid, ca);
        }

        baseLeaf += 2;

        return baseLeaf;
    }

    private void updateCounters(HashMap<Integer, HashMap<String, Integer>> classCounters,
                                ClassList classList) throws IOException {
        String[] line;
        Iterator<String[]> iterator = classAttributeProcessor.getIterator();
        while (iterator.hasNext()) {
            line = iterator.next();

            HashMap<String, Integer> counters;
            int index = classList.getLeafOf(line[0]);
            if (!classCounters.containsKey(index)) {
                counters = new HashMap<>();
                classCounters.put(index, counters);
            } else {
                counters = classCounters.get(index);
            }

            counters.put(line[1], counters.getOrDefault(line[1], 0) + 1);
        }
    }

    private void sortAttributes() {
        Arrays.stream(attributeFileProcessors)
                .filter(attributeFileProcessor -> !attributeFileProcessor.isClassAttribute())
                .forEach(attributeFileProcessor -> attributeFileProcessor.sortAttribute());
    }

    private void splitInFiles(String sourcePath) throws IOException {
        Arrays.stream(attributeFileProcessors).forEach(attributeFileProcessor -> attributeFileProcessor.init());
        try (CSVReader csvReader = new CSVReader(new BufferedReader(new FileReader(sourcePath)))) {
            String[] line;
            int rid = 0;
            while ((line = csvReader.readNext()) != null) {
                for (int i = 0; i < line.length; i++) {
                    if (attributeFileProcessors[i] != null) {
                        attributeFileProcessors[i].addRow(rid, line[i]);
                    }
                }
                rid++;
            }
        }
        Arrays.stream(attributeFileProcessors).forEach(attributeFileProcessor -> attributeFileProcessor.close());
    }

    static class Logarithm {
        public static BigDecimal logx(double x, double y) {
            if (x == 0.0) return BigDecimal.ZERO;
            return BigDecimal.valueOf(
                    Math.log(x)).divide(BigDecimal.valueOf(Math.log(y)),
                    RoundingMode.HALF_EVEN);
        }

        public static BigDecimal log2(double a) {
            return logx(a, 2);
        }
    }
}
