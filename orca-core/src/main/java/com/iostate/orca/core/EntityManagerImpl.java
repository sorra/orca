package com.iostate.orca.core;

import com.iostate.orca.api.ConnectionProvider;
import com.iostate.orca.api.EntityManagerFactory;
import com.iostate.orca.api.EntityObject;
import com.iostate.orca.api.exception.EntityNotFoundException;
import com.iostate.orca.api.exception.NonUniqueResultException;
import com.iostate.orca.api.exception.PersistenceException;
import com.iostate.orca.metadata.AssociationField;
import com.iostate.orca.metadata.BelongsTo;
import com.iostate.orca.metadata.EntityModel;
import com.iostate.orca.metadata.FetchType;
import com.iostate.orca.metadata.Field;
import com.iostate.orca.metadata.HasMany;
import com.iostate.orca.metadata.HasOne;
import com.iostate.orca.metadata.ManyToMany;
import com.iostate.orca.metadata.MetadataManager;
import com.iostate.orca.sql.SqlHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class EntityManagerImpl implements InternalEntityManager {

    private final MetadataManager metadataManager;

    private final SqlHelper sqlHelper;

    public EntityManagerImpl(MetadataManager metadataManager, ConnectionProvider connectionProvider) {
        this.metadataManager = metadataManager;
        this.sqlHelper = new SqlHelper(connectionProvider, this);
    }

    public EntityManagerImpl asDefault() {
        EntityManagerFactory.setDefault(this);
        return this;
    }

    @Override
    public void persist(EntityObject entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        if (isPersisted(entity)) {
            throw new IllegalStateException("entity is already persisted thus unable to persist");
        }

        EntityModel entityModel = getEntityModel(entity);
        sqlHelper.insert(entityModel, entity);

//    refresh(entity);
    }

    @Override
    public void update(EntityObject entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        if (!isPersisted(entity)) {
            throw new IllegalStateException("entity is not persisted thus unable to update");
        }

        EntityModel entityModel = getEntityModel(entity);
        sqlHelper.update(entityModel, entity);

//    refresh(entity);
    }

    @Override
    public void merge(EntityObject entity) {
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
    public void remove(Class<? extends EntityObject> entityClass, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        EntityModel entityModel = metadataManager.findEntityByClass(entityClass);
        sqlHelper.deleteById(entityModel, id);
    }

    @Override
    public void remove(EntityObject entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        EntityModel entityModel = getEntityModel(entity);
        sqlHelper.deleteEntity(entityModel, entity);
    }

    @Override
    public void refresh(EntityObject entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }

        EntityModel entityModel = getEntityModel(entity);
        Object id = entityModel.getIdField().getValue(entity);
        if (id == null) {
            throw new EntityNotFoundException("entityName=" + entityModel.getName() + ", id is null");
        }

        EntityObject from = find(entityModel, id);
        if (from == null) {
            throw new EntityNotFoundException("entityName=" + entityModel.getName() + ", id=" + id);
        }
        // HashSet low performance because of hashCode implementation
        doRefresh(new HashSet<>(), entityModel, entity, from);
    }

    @SuppressWarnings("unchecked")
    private void doRefresh(Set<EntityObject> cache, EntityModel entityModel, EntityObject to, EntityObject from) {
        if (!cache.add(to)) {
            return; // Do not refresh the same object again
        }
        Set<String> updatedFields = to.get_updatedFields();
        for (Field field : entityModel.allFields()) {
            if (updatedFields.contains(field.getName())) {
                continue; // Do not revert an updated field
            }
            Object fromValue = field.getValue(from);
            if (fromValue == null) {
                field.populateValue(to, fromValue);
            } else if (field instanceof AssociationField af) {
                if (!af.cascadeConfig().isRefresh()) {
                    continue;
                }
                if (af.isPlural()) {
                    Collection<EntityObject> fromTargets = (Collection<EntityObject>) fromValue;
                    Collection<EntityObject> toTargets = (Collection<EntityObject>) field.getValue(to);
                    if (fromTargets.isEmpty()) {
                        toTargets.clear();
                    } else {
                        EntityModel targetModel = af.getTargetModelRef().model();
                        Collection<EntityObject> newTargets = new ArrayList<>(fromTargets.size());
                        for (EntityObject fromTarget : fromTargets) {
                            Optional<EntityObject> optional = toTargets.stream().filter(fromTarget::equals).findAny();
                            if (optional.isPresent()) {
                                EntityObject toTarget = optional.get();
                                doRefresh(cache, targetModel, toTarget, fromTarget);
                                newTargets.add(toTarget);
                            } else {
                                newTargets.add(fromTarget);
                            }
                        }
                        toTargets.clear();
                        toTargets.addAll(newTargets);
                    }
                } else {
                    EntityModel targetModel = af.getTargetModelRef().model();
                    Object toValue = field.getValue(to);
                    doRefresh(cache, targetModel, (EntityObject) toValue, (EntityObject) fromValue);
                }
            } else {
                field.populateValue(to, fromValue);
            }
        }
    }

    @Override
    public <T extends EntityObject> T find(Class<T> entityClass, Object id) {
        EntityModel entityModel = metadataManager.findEntityByClass(entityClass);
        //noinspection unchecked
        return (T) find(entityModel, id);
    }

    @Override
    public EntityObject find(String modelName, Object id) {
        EntityModel entityModel = metadataManager.findEntityByName(modelName);
        return find(entityModel, id);
    }

    @Override
    public EntityObject find(EntityModel entityModel, Object id) {
        Objects.requireNonNull(entityModel);
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        return sqlHelper.findById(entityModel, id);
    }

    @Override
    public <T extends EntityObject> List<T> findAll(Class<T> entityClass) {
        EntityModel entityModel = metadataManager.findEntityByClass(entityClass);
        //noinspection unchecked
        return (List<T>) sqlHelper.findAll(entityModel);
    }

    @Override
    public List<EntityObject> findAll(String modelName) {
        EntityModel entityModel = metadataManager.findEntityByName(modelName);
        return sqlHelper.findAll(entityModel);
    }

    @Override
    public <T extends EntityObject> List<T> findBy(Class<T> entityClass, String objectPath, Object fieldValue) {
        EntityModel entityModel = metadataManager.findEntityByClass(entityClass);
        //noinspection unchecked
        return (List<T>) sqlHelper.findBy(entityModel, objectPath, fieldValue);
    }

    @Override
    public List<EntityObject> findBy(String modelName, String objectPath, Object fieldValue) {
        EntityModel entityModel = metadataManager.findEntityByName(modelName);
        return sqlHelper.findBy(entityModel, objectPath, fieldValue);
    }

    @Override
    public void loadLazyField(EntityObject entity, String fieldName) {
        EntityModel entityModel = getEntityModel(entity);
        Field loadedField = entityModel.findFieldByName(fieldName);
        if (loadedField instanceof AssociationField a && a.getFetchType() == FetchType.LAZY) {
            EntityModel targetModel = a.getTargetModelRef().model();
            Field mappedByField = a.getMappedByField();

            if (a instanceof BelongsTo) {
                Object fkValue = entity.getForeignKeyValue(a.getColumnName());
                if (fkValue != null) {
                    Object target = find(targetModel, fkValue);
                    a.populateValue(entity, target);
                }
            } else if (a instanceof HasOne) {
                Object id = entityModel.getIdField().getValue(entity);
                List<EntityObject> targets = sqlHelper.findBy(targetModel, mappedByField.getName(), id);
                if (targets.size() == 1) {
                    a.populateValue(entity, targets.get(0));
                } else if (targets.size() > 1) {
                    throw new NonUniqueResultException(entityModel.getName(), id);
                }
                // else: remain null if targets is empty
            } else if (a instanceof HasMany) {
                Object id = entityModel.getIdField().getValue(entity);
                List<EntityObject> targets = sqlHelper.findBy(targetModel, mappedByField.getName(), id);
                for (EntityObject target : targets) {
                    mappedByField.populateValue(target, entity);
                }
                a.populateValue(entity, targets);
            } else if (a instanceof ManyToMany) {
                Object id = entityModel.getIdField().getValue(entity);
                List<EntityObject> targets = sqlHelper.findManyToManyTargets((ManyToMany) a, id);
                a.populateValue(entity, targets);
            } else {
                throw new PersistenceException("Unknown association type: " + a.getClass());
            }
        }
    }

    @Override
    public SqlHelper getSqlHelper() {
        return sqlHelper;
    }

    private EntityModel getEntityModel(EntityObject entity) {
        if (entity instanceof CommonEntityObject) {
            return metadataManager.findEntityByName(((CommonEntityObject) entity).getModelName());
        } else {
            return metadataManager.findEntityByClass(entity.getClass());
        }
    }

    private boolean isPersisted(EntityObject entity) {
        return entity.persisted();
    }
}
