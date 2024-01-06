package com.iostate.orca.metadata;

public class ReferenceDataType implements DataType {
    private final EntityModelRef targetModelRef;
    private final boolean isPlural;

    public ReferenceDataType(EntityModelRef targetModelRef, boolean isPlural) {
        this.targetModelRef = targetModelRef;
        this.isPlural = isPlural;
    }

    @Override
    public String name() {
        if (isPlural) {
            return "<" + targetModelRef.getName() + ">";
        } else {
            return targetModelRef.getName();
        }
    }

    @Override
    public String javaTypeName() {
        if (isPlural) {
            return "java.util.List" + name();
        } else {
            return name();
        }
    }

    public EntityModelRef getTargetModelRef() {
        return targetModelRef;
    }
}
