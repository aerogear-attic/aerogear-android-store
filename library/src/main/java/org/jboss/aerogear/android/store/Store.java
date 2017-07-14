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
package org.jboss.aerogear.android.store;

import org.jboss.aerogear.android.core.ReadFilter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Represents an abstraction layer for a storage system.
 */
public interface Store<T> {

    /**
     * Reads all the data from the underlying storage system.
     * 
     * @return List of T
     */
    public Collection<T> readAll();

    /**
     * Reads a specific object/record from the underlying storage system.
     * 
     * @param id id from the desired object
     * @return T
     */
    public T read(Serializable id);

    /**
     * Search for objects/records from the underlying storage system.
     * 
     * @param filter a filter to use to fetch an object
     * @return a list of elements, should not be null.
     */
    public List<T> readWithFilter(ReadFilter filter);

    /**
     * Saves the given object in the underlying storage system.
     * 
     * @param item Object to save
     */
    public void save(T item);

    /**
     * Saves the given objects in the underlying storage system.
     *
     * @param items List of objects to save
     */
    public void save(Collection<T> items);

    /**
     * Resets the entire storage system.
     */
    public void reset();

    /**
     * Removes a specific object/record from the underlying storage system.
     * 
     * @param id Id of item to remote
     */
    public void remove(Serializable id);

    /**
     * Checks if the storage system contains no stored elements.
     * 
     * @return true if the storage is empty, otherwise false.
     */
    public boolean isEmpty();

}
