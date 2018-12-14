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

@RequiredArgsConstructor
public class ClassListLocal implements ClassList {

    @NonNull
    private AttributeFileProcessor classAttributeProcessor;

    private List<ClassAttribute> classList;

    @PostConstruct
    public void init() {
        classList = new ArrayList<>();
    }

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

    @Override
    public Integer getLeafOf(String rid) {
        return getClassAttributeOf(rid).getLeaf();
    }

    @Override
    public ClassAttribute getClassAttributeOf(String rid) {
        return classList.get(Integer.valueOf(rid));
    }

    @Override
    public void updateClassAttribute(String rid, ClassAttribute classAttribute) {
        getClassAttributeOf(rid).update(classAttribute);
    }
}
