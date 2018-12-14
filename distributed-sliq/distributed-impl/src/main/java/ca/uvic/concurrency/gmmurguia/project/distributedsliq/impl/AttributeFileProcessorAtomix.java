package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessor;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.core.set.AsyncDistributedSortedSet;
import io.atomix.core.set.DistributedSortedSet;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@RequiredArgsConstructor
public class AttributeFileProcessorAtomix implements AttributeFileProcessor {

    public static AtomicInteger portCounter = new AtomicInteger(0);

    @NonNull
    private String attributeName;

    private boolean classAttribute;

    public static Atomix atomix;

    @NonNull
    private EntropyProcessor entropyProcessor;

    public AttributeFileProcessorAtomix(@NonNull String attributeName, boolean classAttribute) {
        this.attributeName = attributeName;
        this.classAttribute = classAttribute;
    }

    @Override
    public void init() {
        int counter = portCounter.incrementAndGet();
        if (atomix == null) {
            atomix = Atomix.builder()
                    .withMemberId("client" + counter)
                    .withAddress("localhost:" + (counter + 6000))
                    .withMembershipProvider(BootstrapDiscoveryProvider.builder()
                            .withNodes(
                                    Node.builder()
                                            .withId("member-1")
                                            .withAddress("localhost:5000")
                                            .build(),
                                    Node.builder()
                                            .withId("member-2")
                                            .withAddress("localhost:5001")
                                            .build())
                            .build())
                    .withPartitionGroups(RaftPartitionGroup.builder("data")
                            .withMembers("member-1", "member-2")
                            .build())
                    .build();
            atomix.start().join();
        }

        atomix.getSortedSet(attributeName)
                .clear();
    }

    private List<CompletableFuture> futures = new ArrayList<>();

    @Override
    public void addRow(Integer index, String value) {
        String element;
        if (isClassAttribute()) {
            element = String.format("%d,%s", index, value);
        } else {
            element = String.format("%s,%d", value, index);
        }
        DistributedSortedSet<String> ss = atomix.getSortedSet(attributeName);
        AsyncDistributedSortedSet<String> adss = ss.async();
        CompletableFuture cf = adss.add(element);
        futures.add(cf);
        if (futures.size() == 1000) {
            futures.forEach(CompletableFuture::join);
            futures.clear();
        }
    }

    @Override
    public Iterator<String[]> getIterator() {
        return new Iterator<String[]>() {
            DistributedSortedSet<String> ss = atomix.getSortedSet(attributeName);
            Iterator<String> it = ss.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public String[] next() {
                String[] parts = it.next().split(",");
                if (isClassAttribute()) {
                    return parts;
                } else {
                    return new String[]{parts[1], parts[0]};
                }
            }
        };
    }

    @Override
    public void sortAttribute() {
        // It is already sorted
    }

    @Override
    public void close() {
        // Should not close the client
    }
}
