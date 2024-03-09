package com.iostate.example.persistence;

import com.iostate.example.persistence.entity.Item;
import com.iostate.orca.api.EntityManager;
import com.iostate.orca.api.PersistentObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ItemRepository {
    @Autowired
    private EntityManager entityManager;

    public Item find(Long id) {
        return entityManager.find(Item.class, id);
    }

    public Item save(Item item) {
        entityManager.persist(item);
        return item;
    }

    public PersistentObject save(PersistentObject po) {
        entityManager.persist(po);
        return po;
    }
}
