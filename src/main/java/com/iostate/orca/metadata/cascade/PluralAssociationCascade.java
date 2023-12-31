package com.iostate.orca.metadata.cascade;


import com.iostate.orca.api.EntityManager;
import com.iostate.orca.metadata.CascadeConfig;
import com.iostate.orca.api.PersistentObject;
import com.iostate.orca.metadata.PluralAssociationField;
import com.iostate.orca.metadata.inverse.DirectInverse;
import com.iostate.orca.metadata.inverse.IndirectInverse;
import com.iostate.orca.metadata.inverse.Inverse;

import java.util.Collection;

public class PluralAssociationCascade implements Cascade {

    private final PluralAssociationField field;
    private final Collection<PersistentObject> values;
    private final CascadeConfig cascadeConfig;

    public PluralAssociationCascade(
            PluralAssociationField field,
            Collection<PersistentObject> values,
            CascadeConfig cascadeConfig) {
        this.field = field;
        this.values = values;
        this.cascadeConfig = cascadeConfig;
    }

    @Override
    public void persist(EntityManager entityManager) {
        if (cascadeConfig.isPersist() && values != null) {
            for (Object value : values) {
                // Should use merge?
                entityManager.merge(value);
            }
        }
    }

    @Override
    public void merge(EntityManager entityManager) {
        if (cascadeConfig.isMerge() && values != null) {
            for (Object value : values) {
                entityManager.merge(value);
            }
        }
    }

    @Override
    public void remove(EntityManager entityManager) {
        if (cascadeConfig.isRemove() && values != null) {
            for (Object value : values) {
                entityManager.remove(value);
            }
        }
    }

    @Override
    public Inverse getInverse(EntityManager entityManager) {
        if (field.getTargetInverseField() != null) {
            return new DirectInverse(field.getTargetInverseField(), values);
        } else {
            return new IndirectInverse(field.getMiddleTable(), values, entityManager);
        }
    }
}
