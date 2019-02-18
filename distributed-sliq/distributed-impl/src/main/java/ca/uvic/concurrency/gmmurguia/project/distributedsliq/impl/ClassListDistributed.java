package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.ClassAttribute;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.ClassList;
import io.atomix.core.Atomix;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

/**
 * The distributed implementation of the class list, backed up by an Atomix distributed primitive.
 */
@RequiredArgsConstructor
public class ClassListDistributed implements ClassList {

    @NonNull
    private String attributeName;

    private static Atomix atomix;

    /**
     * Meant to be called right after the object is constructed and either create a new Atomix instance or copy that
     * of the <code>AttributeFileProcessorAtomix</code>. After testing, it's better to avoid creating too many members
     * of the cluster to avoid too much overhead.
     */
    @PostConstruct
    public void init() {
        atomix = AttributeFileProcessorAtomix.atomix;
    }

    /**
     * Moves the data from the attribute distributed primitive to the class's.
     */
    @Override
    public void fill() {
        if (atomix == null) {
            atomix = AttributeFileProcessorAtomix.atomix;
        }
        Map<Integer, ClassAttribute> classList = atomix.getMap(getClassListName());
        SortedSet<String> attrValues = atomix.getSortedSet(attributeName);
        // remove any leftovers
        classList.clear();

        Iterator<String> it = attrValues.iterator();
        while (it.hasNext()) {
            String[] parts = it.next().split(",");
            classList.put(Integer.valueOf(parts[0]), new ClassAttribute(parts[0], parts[1], 0));
        }
    }

    /**
     * Obtains the leaf value of the requested row ID. Note that leaf values are what will later be used by the
     * decision tree.
     *
     * @param rid the row ID to be queried.
     * @return the leaf value of the row ID.
     */
    @Override
    public Integer getLeafOf(String rid) {
        return getClassAttributeOf(rid).getLeaf();
    }

    /**
     * Returns the @{@link ClassAttribute} of the requested row ID.
     *
     * @param rid the row ID to be queried.
     * @return the {@link ClassAttribute} of the row ID.
     */
    @Override
    public ClassAttribute getClassAttributeOf(String rid) {
        Map<Integer, ClassAttribute> classList = atomix.getMap(getClassListName());
        return classList.get(Integer.valueOf(rid));
    }

    /**
     * Updates the {@link ClassAttribute} at the specified row ID.
     *
     * @param rid the row ID of the {@link ClassAttribute} to be updated.
     * @param classAttribute the new value of the {@link ClassAttribute}.
     */
    @Override
    public void updateClassAttribute(String rid, ClassAttribute classAttribute) {
        Map<Integer, ClassAttribute> classList = atomix.getMap(getClassListName());
        classList.put(Integer.valueOf(rid), classAttribute);
    }

    private String getClassListName() {
        return "cl_" + attributeName;
    }
}
