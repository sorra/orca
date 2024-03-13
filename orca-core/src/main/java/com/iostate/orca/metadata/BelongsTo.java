package com.iostate.orca.metadata;

import com.iostate.orca.api.EntityObject;
import com.iostate.orca.metadata.cascade.Cascade;
import com.iostate.orca.metadata.cascade.VoidCascade;

// Backed by FK
public class BelongsTo extends AssociationField {

    private final String columnName;
    private final DataType dataType;

    public BelongsTo(
            String name, String columnName,
            EntityModel sourceModel, EntityModelRef targetModelRef,
            boolean isNullable, FetchType fetchType, CascadeType[] cascadeTypes) {
        super(name, sourceModel, targetModelRef, null, isNullable, fetchType, cascadeTypes);
        this.columnName = columnName;
        this.dataType = new ReferentialDataType(targetModelRef, false);
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public Cascade getCascade(EntityObject entity) {
        return new VoidCascade();
//        return new SingularAssociationCascade(this, (PersistentObject) getValue(entity), cascadeConfig());
    }

    @Override
    public boolean isSingular() {
        return true;
    }

    @Override
    public boolean isPlural() {
        return false;
    }
}
