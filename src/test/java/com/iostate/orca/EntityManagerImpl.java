package com.iostate.orca;

import com.iostate.orca.api.ConnectionProvider;
import com.iostate.orca.api.EntityManager;
import com.iostate.orca.api.PersistentObject;
import com.iostate.orca.metadata.AssociationField;
import com.iostate.orca.metadata.EntityModel;
import com.iostate.orca.metadata.FetchType;
import com.iostate.orca.metadata.Field;
import com.iostate.orca.metadata.MetadataManager;
import com.iostate.orca.metadata.PluralAssociationField;
import com.iostate.orca.sql.SqlHelper;

import javax.persistence.EntityNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class EntityManagerImpl implements EntityManager {

    private final MetadataManager metadataManager;

    private final SqlHelper sqlHelper;

    public EntityManagerImpl(MetadataManager metadataManager, ConnectionProvider connectionProvider) {
        this.metadataManager = metadataManager;
        this.sqlHelper = new SqlHelper(connectionProvider, this);
    }

    @Override
    public void persist(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        if (isPersisted(entity)) {
            throw new IllegalStateException("entity is already persisted thus unable to persist");
        }

        EntityModel entityModel = getEntityModel(entity.getClass());
        sqlHelper.insert(entityModel, (PersistentObject) entity);

//    refresh(entity);
    }

    @Override
    public void update(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        if (!isPersisted(entity)) {
            throw new IllegalStateException("entity is not persisted thus unable to update");
        }

        EntityModel entityModel = getEntityModel(entity.getClass());
        sqlHelper.update(entityModel, (PersistentObject) entity);

//    refresh(entity);
    }

    @Override
    public void merge(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        if (isPersisted(entity)) {
            update(entity);
        } else {
            persist(entity);
        }
    }

    @Override
    public void remove(Class<?> entityClass, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        EntityModel entityModel = getEntityModel(entityClass);
        sqlHelper.delete(entityModel, id);
    }

    @Override
    public void remove(Object entity) {
        EntityModel entityModel = getEntityModel(entity.getClass());
        Object id = entityModel.getIdField().getValue(entity);
        sqlHelper.delete(entityModel, id);
    }

    @Override
    public void refresh(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        EntityModel entityModel = getEntityModel(entity.getClass());
        Object id = entityModel.getIdField().getValue(entity);
        if (id == null) {
            throw new EntityNotFoundException("entityName=" + entityModel.getName() + ", id is null");
        }

        Object from = find(entity.getClass(), id);
        if (from == null) {
            throw new EntityNotFoundException("entityName=" + entityModel.getName() + ", id=" + id);
        }

//    try {
//      BeanUtils.copyProperties(entity, from);
//    } catch (IllegalAccessException | InvocationTargetException e) {
//      throw new PersistenceException("Failed to copyProperties", e);
//    }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        EntityModel entityModel = getEntityModel(entityClass);
        PersistentObject po = find(entityModel, id);

        //noinspection unchecked
        return (T) po;
    }

    @Override
    public PersistentObject find(EntityModel entityModel, Object id) {
        Objects.requireNonNull(entityModel);
        PersistentObject po = sqlHelper.findById(entityModel, id);

        // Populate references
        entityModel.allFields()
                .stream()
                .filter(Field::isAssociation)
                .forEach(field -> {
                    AssociationField a = (AssociationField) field;
                    if (a.getFetchType() == FetchType.EAGER) {
                        return;
                    }
                    if (a.isSingular()) {
                        Object raw = po.getFieldValue(a.getName());
                        if (raw != null) {
                            Object targetObject = find(a.getTargetModelRef().model(), raw);
                            field.setValue(po, targetObject);
                        }
                    } else {
                        PluralAssociationField pa = (PluralAssociationField) a;
                        List<PersistentObject> targets;
                        if (pa.getTargetInverseField() != null) {
                            targets = sqlHelper.findByField(a.getTargetModelRef().model(), a.getTargetInverseField(), id);
                            for (PersistentObject target : targets) {
                                pa.getTargetInverseField().setValue(target, id);
                            }
                        } else {
                            targets = sqlHelper.findByRelation(pa.getMiddleTable(), id);
                        }
                        //TODO should also load target's associations
                        field.setValue(po, targets);
                    }
                });
        return po;
    }

    @Override
    public <T> List<T> findByField(Class<T> entityClass, String fieldName, Object fieldValue) {
        EntityModel entityModel = getEntityModel(entityClass);
        Field field = entityModel.findFieldByName(fieldName);
        if (field == null) {
            throw new IllegalArgumentException(String.format("field %s is not found in class %s", fieldName, entityClass.getName()));
        }

        //noinspection unchecked
        return (List<T>) sqlHelper.findByField(entityModel, field, fieldValue);
    }

    @Override
    public <T> T fetch(Class<T> entityClass, Object id) {
        return null;
    }

    @Override
    public <T> T ref(Class<T> entityClass, Object id) {
        return null;
    }

    @Override
    public int executeDML(String sql, Object[] args) throws SQLException {
        return sqlHelper.executeDML(sql, args);
    }

    private EntityModel getEntityModel(Class<?> entityClass) {
        return metadataManager.findEntityByClass(entityClass);
    }

    private boolean isPersisted(Object entity) {
        return ((PersistentObject) entity).isPersisted();
    }
}
