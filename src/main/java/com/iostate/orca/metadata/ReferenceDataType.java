package com.iostate.orca.metadata;

public class ReferenceDataType implements DataType {
    private final EntityModelRef targetModel;
    private final Class<?> containerClass;

    public ReferenceDataType(EntityModelRef targetModel, boolean isPlural) {
        this.targetModel = targetModel;
        this.containerClass = isPlural ? java.util.List.class : null;
    }

    @Override
    public String name() {
        return javaTypeName();
    }

    @Override
    public String javaTypeName() {
        if (containerClass == null) {
            return targetModel.getName();
        } else {
            return containerClass.getName() + "<" + targetModel.getName() + ">";
        }
    }
}
