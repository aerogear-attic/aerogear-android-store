/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.store.memory;

import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.store.generator.IdGenerator;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.core.reflection.Property;
import org.jboss.aerogear.android.core.reflection.Scan;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

/**
 * Memory implementation of Store {@link Store}.
 */
public class MemoryStore<T> implements Store<T> {

    private final Map<Serializable, T> data = new HashMap<Serializable, T>();
    private final IdGenerator idGenerator;

    public MemoryStore(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> readAll() {
        return data.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T read(Serializable id) {
        return data.get(id);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void save(T item) {
        Serializable idValue = getOrGenerateIdValue(item);
        save(idValue, item);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void save(Collection<T> items) {
        for (T item : items) {
            save(item);
        }
    }

    void save(Serializable id, T item) {
        data.put(id, item);
    }

    Serializable getOrGenerateIdValue(Object data) {
        String recordIdFieldName = Scan.recordIdFieldNameIn(data.getClass());

        Property property = new Property(data.getClass(), recordIdFieldName);

        Serializable idValue = (Serializable) property.getValue(data);

        if (idValue == null) {
            idValue = idGenerator.generate();
            property.setValue(data, idValue);
        }

        return idValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        data.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Serializable id) {
        data.remove(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException if filter.query has nested objects
     */
    @Override
    public List<T> readWithFilter(ReadFilter filter) {
        if (filter == null) {
            filter = new ReadFilter();
        }
        JSONObject where = filter.getWhere();
        scanForNestedObjectsInWhereClause(where);
        List<T> results = new ArrayList<T>(data.values());

        filterData(results, where);
        results = pageData(results, filter.getLimit(), filter.getOffset());
        return results;
    }

    private void scanForNestedObjectsInWhereClause(JSONObject where) {
        String key;
        Object value;
        Iterator keys = where.keys();
        while (keys.hasNext()) {
            key = keys.next().toString();
            value = where.opt(key);
            if (value instanceof JSONObject) {
                throw new IllegalArgumentException("readWithFilter does not support nested objects");
            }
        }
    }

    private void filterData(Collection<T> data, JSONObject where) {
        String filterPropertyName;
        Object filterValue;
        Iterator keys = where.keys();
        while (keys.hasNext()) {
            ArrayList toRemove = new ArrayList(data.size()); // We will not remove more items than are in data
            filterPropertyName = keys.next().toString();
            filterValue = where.opt(filterPropertyName);

            for (T objectInStorage : data) {
                Property objectProperty = new Property(objectInStorage.getClass(), filterPropertyName);
                Object propertyValue = objectProperty.getValue(objectInStorage);
                if (propertyValue != null && filterValue != null) {
                    if (!propertyValue.equals(filterValue)) {
                        toRemove.add(objectInStorage);
                    }
                }
            }
            data.removeAll(toRemove);
        }
    }

    private List<T> pageData(List<T> results, Integer limit, Integer offset) {
        return results.subList(offset, Math.min(offset + limit, results.size()));
    }
}
