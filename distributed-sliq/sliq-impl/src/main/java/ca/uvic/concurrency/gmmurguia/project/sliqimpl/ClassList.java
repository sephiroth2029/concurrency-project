package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

/**
 * This is the data structure proposed by SLIQ. It holds the list of the class attribute.
 */
public interface ClassList {

    /**
     * Fills the data structure.
     */
    void fill();

    /**
     * Obtains the leaf value of the given row ID.
     *
     * @param rid the row ID.
     * @return the leaf value of the given row ID.
     */
    Integer getLeafOf(String rid);

    /**
     * Obtains the {@link ClassAttribute} associated with the given row ID.
     *
     * @param rid the target row ID.
     * @return the {@link ClassAttribute} associated with the given row ID.
     */
    ClassAttribute getClassAttributeOf(String rid);

    /**
     * Updates the {@link ClassAttribute} values for the given row ID.
     *
     * @param rid the target row ID.
     * @param classAttribute the new values.
     */
    void updateClassAttribute(String rid, ClassAttribute classAttribute);

}
