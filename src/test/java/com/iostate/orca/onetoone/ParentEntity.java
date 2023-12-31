package com.iostate.orca.onetoone;

import java.util.Collections;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@com.iostate.orca.api.Namespace("onetoone")
public class ParentEntity extends com.iostate.orca.api.BasePO {
    private Long id;
    private String string;
    private ChildEntity child;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        markUpdatedField("id");
    }
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
        markUpdatedField("string");
    }
    public ChildEntity getChild() {
        return child;
    }

    public void setChild(ChildEntity child) {
        this.child = child;
        markUpdatedField("child");
    }

    private static final Map<String, Function<ParentEntity, Object>> GETTERS;

    static {
        Map<String, Function<ParentEntity, Object>> getters = new HashMap<>();
        getters.put("id", ParentEntity::getId);
        getters.put("string", ParentEntity::getString);
        getters.put("child", ParentEntity::getChild);
        GETTERS = Collections.unmodifiableMap(getters);
    }

    private static final Map<String, BiConsumer<ParentEntity, Object>> SETTERS;

    static {
        Map<String, BiConsumer<ParentEntity, Object>> setters = new HashMap<>();
        setters.put("id", (object, value) -> object.setId((Long) value));
        setters.put("string", (object, value) -> object.setString((String) value));
        setters.put("child", (object, value) -> object.setChild((ChildEntity) value));
        SETTERS = Collections.unmodifiableMap(setters);
    }

    @Override
    public Object getFieldValue(String name) {
        Objects.requireNonNull(name, "field name must not be null");
        return GETTERS.get(name)
            .apply(this);
    }

    @Override
    public void setFieldValue(String name, Object value) {
        Objects.requireNonNull(name, "field name must not be null");
        SETTERS.get(name)
            .accept(this, value);
        markUpdatedField(name);
    }
}
