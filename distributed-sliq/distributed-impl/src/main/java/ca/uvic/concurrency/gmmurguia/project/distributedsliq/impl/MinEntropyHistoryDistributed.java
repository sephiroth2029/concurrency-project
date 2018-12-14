package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyData;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyHistory;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import org.apache.commons.lang3.RandomUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl.AttributeFileProcessorAtomix.portCounter;

public class MinEntropyHistoryDistributed implements MinEntropyHistory {

    public static final String MIN_ENTROPIES = "minEntropies";
    public static final String MIN_ATTR_VALS = "minAttrVals";
    public static final String MIN_PROCESSORS = "minProcessors";
    private static Atomix atomix;

    @PostConstruct
    public void init() {
        atomix = AttributeFileProcessorAtomix.atomix;
//        int counter = portCounter.incrementAndGet();
//        if (atomix == null) {
//            atomix = Atomix.builder()
//                    .withMemberId("client-" + counter)
//                    .withAddress("localhost:" + (counter + 6000))
//                    .withMembershipProvider(BootstrapDiscoveryProvider.builder()
//                            .withNodes(
//                                    Node.builder()
//                                            .withId("member-1")
//                                            .withAddress("localhost:5000")
//                                            .build(),
//                                    Node.builder()
//                                            .withId("member-2")
//                                            .withAddress("localhost:5001")
//                                            .build())
//                            .build())
//                    .withPartitionGroups(RaftPartitionGroup.builder("data")
//                            .withMembers("member-1", "member-2")
//                            .build())
//                    .build();
//            atomix.start().join();
//        }
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

    @Override
    public void clear() {
        if (atomix == null) {
            atomix = AttributeFileProcessorAtomix.atomix;
        }
        atomix.getMap(MIN_ATTR_VALS).clear();
        atomix.getMap(MIN_ENTROPIES).clear();
        atomix.getMap(MIN_PROCESSORS).clear();
    }

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

    @Override
    public void updateMinEntropiesFor(Integer leaf, MinEntropyData minEntropyData) {
        getMinEntropies().put(leaf, minEntropyData.getMinEntropy());
        getMinAttrVals().put(leaf, minEntropyData.getMinAttrVals());
        getMinProcessors().put(leaf, minEntropyData.getMinProcessor());
    }

    @Override
    public Collection<Integer> getMinEntropiesLeaves() {
        return getMinEntropies().keySet();
    }

    @Override
    public String getMinProcessorName(Integer leaf) {
        return getMinProcessors().get(leaf);
    }

    @Override
    public String[] getMinAttrVals(Integer leaf) {
        return getMinAttrVals().get(leaf);
    }
}
