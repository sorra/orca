package com.iostate.orca.sql.query;

import com.iostate.orca.api.PersistentObject;
import com.iostate.orca.metadata.AssociationField;
import com.iostate.orca.metadata.EntityModel;
import com.iostate.orca.metadata.Field;
import com.iostate.orca.sql.ResultMapper;
import com.iostate.orca.sql.type.TypeHandlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * Common logic to fully map row to entity
 */
class QueryResultMapper implements ResultMapper {
    private final EntityModel model;
    private final FieldSelection fieldSelection;

    QueryResultMapper(EntityModel model, FieldSelection fieldSelection) {
        this.model = model;
        this.fieldSelection = fieldSelection;
    }

    @Override
    public PersistentObject mapRow(ResultSet rs) throws SQLException {
        PersistentObject po = model.newInstance();
        po.setPersisted(true);

        for (SelectedField sf : fieldSelection.getSelectedFields()) {
            Field field = sf.getField();
            if (field.isAssociation()) {
                EntityModel targetModel = ((AssociationField) field).getTargetModelRef().model();
                Field targetIdField = targetModel.getIdField();
                Object targetId = TypeHandlers.INSTANCE.find(targetIdField.getDataType())
                        .getValue(rs, sf.getIndex());
                if (isValidId(targetId)) {
                    po.setForeignKeyValue(field.getColumnName(), targetId);
                    PersistentObject target = targetModel.newInstance();
                    targetIdField.setValue(target, targetId);
                    po.setFieldValue(field.getName(), target);
                }
            } else {
                Object value = TypeHandlers.INSTANCE.find(field.getDataType())
                        .getValue(rs, sf.getIndex());
                po.setFieldValue(field.getName(), value);
            }
        }

        return po;
    }

    public Object getId(ResultSet rs) throws SQLException {
        SelectedField sf = fieldSelection.getIdField();
        return TypeHandlers.INSTANCE.find(sf.getField().getDataType())
                .getValue(rs, sf.getIndex());
    }

    public boolean isValidId(Object id) {
        if (id == null) {
            return false;
        } else if (id instanceof Number) {
            return ((Number) id).longValue() != 0L;
        } else if (id instanceof CharSequence) {
            return ((CharSequence) id).length() != 0;
        } else if (id instanceof Date || id instanceof Temporal) {
            throw new IllegalArgumentException("Unsupported id class: " + id.getClass());
        }
        return true;
    }
}
