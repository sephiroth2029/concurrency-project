package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.ClassAttribute;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.ClassList;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import static ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl.AttributeFileProcessorAtomix.portCounter;

@RequiredArgsConstructor
public class ClassListDistributed implements ClassList {

    @NonNull
    private String attributeName;

    private static Atomix atomix;

    @PostConstruct
    public void init() {
        atomix = AttributeFileProcessorAtomix.atomix;
//        int counter = portCounter.incrementAndGet();
//        atomix = Atomix.builder()
//                .withMemberId("client-" + counter)
//                .withAddress("localhost:" + (counter + 6000))
//                .withMembershipProvider(BootstrapDiscoveryProvider.builder()
//                        .withNodes(
//                                Node.builder()
//                                        .withId("member-1")
//                                        .withAddress("localhost:5000")
//                                        .build(),
//                                Node.builder()
//                                        .withId("member-2")
//                                        .withAddress("localhost:5001")
//                                        .build())
//                        .build())
//                .withPartitionGroups(RaftPartitionGroup.builder("data")
//                        .withMembers("member-1", "member-2")
//                        .build())
//                .build();
//        atomix.start().join();
    }

    @Override
    public void fill() {
        if (atomix == null) {
            atomix = AttributeFileProcessorAtomix.atomix;
        }
        Map<Integer, ClassAttribute> classList = atomix.getMap(getClassListName());
        SortedSet<String> attrValues = atomix.getSortedSet(attributeName);
        classList.clear();
        Iterator<String> it = attrValues.iterator();
        while (it.hasNext()) {
            String[] parts = it.next().split(",");
            classList.put(Integer.valueOf(parts[0]), new ClassAttribute(parts[0], parts[1], 0));
        }
    }

    @Override
    public Integer getLeafOf(String rid) {
        return getClassAttributeOf(rid).getLeaf();
    }

    @Override
    public ClassAttribute getClassAttributeOf(String rid) {
        Map<Integer, ClassAttribute> classList = atomix.getMap(getClassListName());
        return classList.get(Integer.valueOf(rid));
    }

    @Override
    public void updateClassAttribute(String rid, ClassAttribute classAttribute) {
        Map<Integer, ClassAttribute> classList = atomix.getMap(getClassListName());
        classList.put(Integer.valueOf(rid), classAttribute);
    }

    private String getClassListName() {
        return "cl_" + attributeName;
    }
}
