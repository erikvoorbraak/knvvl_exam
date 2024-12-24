package org.knvvl.exam.meta;

public interface LabeledEntity extends IdEntity
{
    String getLabel();

    default String getDisplayString() {
        return getLabel();
    }
}
