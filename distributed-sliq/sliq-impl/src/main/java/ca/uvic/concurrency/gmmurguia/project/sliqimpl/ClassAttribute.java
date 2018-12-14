package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ClassAttribute {

    private String rid;

    private String value;

    private int leaf;

    public void update(ClassAttribute newClassAttribute) {
        this.rid = newClassAttribute.rid;
        this.value = newClassAttribute.value;
        this.leaf = newClassAttribute.leaf;
    }

}
