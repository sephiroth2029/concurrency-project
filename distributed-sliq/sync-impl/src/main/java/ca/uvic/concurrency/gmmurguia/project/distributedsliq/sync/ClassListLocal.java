package ca.uvic.concurrency.gmmurguia.project.distributedsliq.sync;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.ClassAttribute;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.ClassList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains the class list data structure in memory.
 */
@RequiredArgsConstructor
public class ClassListLocal implements ClassList {

    @NonNull
    private AttributeFileProcessor classAttributeProcessor;

    private List<ClassAttribute> classList;

    /**
     * Creates the class list structure.
     */
    @PostConstruct
    public void init() {
        classList = new ArrayList<>();
    }

    /**
     * Moves the attributes to memory.
     */
    @Override
    public void fill() {
        String[] line;
        Iterator<String[]> iterator;
        try {
            iterator = classAttributeProcessor.getIterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (iterator.hasNext()) {
            line = iterator.next();
            classList.add(new ClassAttribute(line[0], line[1], 0));
        }
    }

    /**
     * Obtains the leaf value for the given ID.
     *
     * @param rid the row ID.
     * @return
     */
    @Override
    public Integer getLeafOf(String rid) {
        return getClassAttributeOf(rid).getLeaf();
    }

    /**
     * Obtains the {@link ClassAttribute} for the given row ID.
     *
     * @param rid the target row ID.
     * @return
     */
    @Override
    public ClassAttribute getClassAttributeOf(String rid) {
        return classList.get(Integer.valueOf(rid));
    }

    /**
     * Updates the class attribute value for the given row ID.
     *
     * @param rid            the target row ID.
     * @param classAttribute the new values.
     */
    @Override
    public void updateClassAttribute(String rid, ClassAttribute classAttribute) {
        getClassAttributeOf(rid).update(classAttribute);
    }
}
