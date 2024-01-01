package com.iostate.orca.metadata;

import com.iostate.orca.api.exception.PersistenceException;
import com.iostate.orca.api.PersistentObject;
import com.iostate.orca.sql.SqlHelper;

import java.sql.SQLException;

public class MiddleTable {

    private final String tableName;
    private final EntityModelRef sourceModel;
    private final EntityModelRef targetModel;

    public MiddleTable(String tableName, EntityModelRef sourceModel, EntityModelRef targetModel) {
        this.tableName = tableName;
        this.sourceModel = sourceModel;
        this.targetModel = targetModel;
    }

    public String getTableName() {
        return tableName;
    }

    public EntityModelRef getSourceModel() {
        return sourceModel;
    }

    public EntityModelRef getTargetModel() {
        return targetModel;
    }

    public void put(PersistentObject source, PersistentObject target, SqlHelper sqlHelper) {
        //TODO check existence
        String sql = "INSERT INTO " + tableName + "(source_id, target_id) VALUES(?,?)";
        Object sourceId = sourceModel.model().getIdField().getValue(source);
        if (sourceId == null) {
            throw new PersistenceException("Failed to insert a relationship, sourceId is null");
        }
        Object targetId = targetModel.model().getIdField().getValue(target);
        if (targetId == null) {
            throw new PersistenceException("Failed to insert a relationship, targetId is null");
        }
        try {
            sqlHelper.executeDML(sql, new Object[]{sourceId, targetId});
        } catch (SQLException e) {
            throw new PersistenceException(
                    String.format("Failed to delete the relationship (sourceId=%s, targetId=%s)", sourceId, targetId),
                    e);
        }
    }

    public void remove(PersistentObject source, PersistentObject target, SqlHelper sqlHelper) {
        String sql = "DELETE FROM " + tableName + " WHERE source_id = ? AND target_id = ?";
        Object sourceId = sourceModel.model().getIdField().getValue(source);
        if (sourceId == null) {
            throw new PersistenceException("Failed to delete a relationship, sourceId is null");
        }
        Object targetId = targetModel.model().getIdField().getValue(target);
        if (targetId == null) {
            throw new PersistenceException("Failed to delete a relationship, targetId is null");
        }
        try {
            sqlHelper.executeDML(sql, new Object[]{sourceId, targetId});
        } catch (SQLException e) {
            throw new PersistenceException(
                    String.format("Failed to delete the relationship (sourceId=%s, targetId=%s)", sourceId, targetId),
                    e);
        }
    }
}
