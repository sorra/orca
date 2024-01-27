package com.iostate.orca.onetomany;

import com.iostate.orca.TestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OneToManyAggregateTest extends TestBase {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{ParentEntity.class, ChildEntity.class};
    }

    @Test
    public void testCreateAll() {
        ParentEntity preparedParent = prepare();

        entityManager.persist(preparedParent);

        ParentEntity resultParent = entityManager.find(ParentEntity.class, preparedParent.getId());
        assertNotNull(resultParent.getId());
        assertEquals(2, resultParent.getChildren().size());
        ChildEntity child1 = resultParent.getChildren().get(0);
        ChildEntity child2 = resultParent.getChildren().get(1);
        assertNotNull(child1.getId());
        assertNotNull(child2.getId());
        assertNotEquals(child1.getId(), child2.getId());
    }

    @Test
    public void testUpdateAll() {
        ParentEntity preparedParent = prepare();
        entityManager.persist(preparedParent);

        preparedParent.setString("updated");
        preparedParent.getChildren().get(0).setInteger(1);
        preparedParent.getChildren().get(1).setInteger(2);

        entityManager.update(preparedParent);

        ParentEntity resultParent = entityManager.find(ParentEntity.class, preparedParent.getId());
        assertNotNull(resultParent.getId());
        assertEquals(2, resultParent.getChildren().size());
        ChildEntity child1 = resultParent.getChildren().get(0);
        ChildEntity child2 = resultParent.getChildren().get(1);
        assertNotNull(child1.getId());
        assertNotNull(child2.getId());
        assertNotEquals(child1.getId(), child2.getId());

        assertEquals("updated", resultParent.getString());
        assertEquals(1, child1.getInteger());
        assertEquals(2, child2.getInteger());
    }

    private ParentEntity prepare() {
        ParentEntity preparedParent = new ParentEntity();
        preparedParent.getChildren().add(new ChildEntity());
        preparedParent.getChildren().add(new ChildEntity());
        return preparedParent;
    }
}
