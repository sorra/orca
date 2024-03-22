package com.iostate.orca.sql;

import com.iostate.orca.metadata.EntityModel;
import com.iostate.orca.metadata.Field;
import com.iostate.orca.sql.type.TypeHandlers;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityKeyMapper implements KeyMapper {

    private final EntityModel model;

    public EntityKeyMapper(EntityModel model) {
        this.model = model;
    }

    @Override
    public Object mapKey(ResultSet keySet) throws SQLException {
        Field field = model.getIdField();
        return TypeHandlers.INSTANCE.find(field.getDataType())
                .getValue(keySet, 1, field.isNullable());
    }
}
