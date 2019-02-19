package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * It's the attribute that we want to classify.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class ClassAttribute {

    private String rid;

    private String value;

    private int leaf;

    /**
     * Updates the values of this class attribute with the values of the one provided.
     *
     * @param newClassAttribute
     */
    public void update(ClassAttribute newClassAttribute) {
        this.rid = newClassAttribute.rid;
        this.value = newClassAttribute.value;
        this.leaf = newClassAttribute.leaf;
    }

}
