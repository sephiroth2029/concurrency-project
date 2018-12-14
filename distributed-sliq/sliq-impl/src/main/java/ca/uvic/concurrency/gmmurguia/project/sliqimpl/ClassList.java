package ca.uvic.concurrency.gmmurguia.project.sliqimpl;

public interface ClassList {

    void fill();

    Integer getLeafOf(String rid);

    ClassAttribute getClassAttributeOf(String rid);

    void updateClassAttribute(String rid, ClassAttribute classAttribute);

}
