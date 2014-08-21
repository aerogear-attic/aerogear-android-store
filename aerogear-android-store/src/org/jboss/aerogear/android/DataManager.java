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
package org.jboss.aerogear.android;

import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreFactory;
import org.jboss.aerogear.android.impl.datamanager.DefaultIdGenerator;
import org.jboss.aerogear.android.impl.datamanager.DefaultStoreFactory;
import org.jboss.aerogear.android.impl.datamanager.StoreConfig;
import org.jboss.aerogear.android.impl.datamanager.StoreTypes;

import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an abstraction layer for a storage system.
 * 
 * As a note, you should NOT extend this class for production or application
 * purposes. This class is made non-final ONLY for testing/mocking/academic
 * purposes.
 */
public class DataManager {

    private final Map<String, Store> stores = new HashMap<String, Store>();
    /**
     * This will default to {@link DefaultIdGenerator} if not provided.
     */
    private final IdGenerator idGenerator;
    /**
     * This will default to {@link DefaultStoreFactory} if not provided.
     */
    private final StoreFactory storeFactory;

    /**
     * Creates a new DataManager using {@link  DefaultIdGenerator} and
     * {@link DefaultStoreFactory}
     */
    public DataManager() {
        this(new DefaultIdGenerator(), new DefaultStoreFactory());
    }

    /**
     * Creates a new DataManager using the idGenerator parameter and
     * {@link DefaultStoreFactory}
     *
     * @param idGenerator an idGenerator
     * @throws IllegalArgumentException if idGenerator is null
     */
    public DataManager(IdGenerator idGenerator) {
        this(idGenerator, new DefaultStoreFactory());
    }

    /**
     * Creates a new DataManager using the storeFactory parameter and
     * {@link DefaultIdGenerator}
     *
     * @param storeFactory a store factory
     * @throws IllegalArgumentException if storeFactory is null
     */
    public DataManager(StoreFactory storeFactory) {
        this(new DefaultIdGenerator(), storeFactory);
    }

    /**
     * Creates a DataManager using the supplied parameters
     *
     * @param idGenerator an idGenerator
     * @param storeFactory a store factory
     * @throws IllegalArgumentException if idGenerator is null
     * @throws IllegalArgumentException if storeFactory is null
     */
    public DataManager(IdGenerator idGenerator, StoreFactory storeFactory) {
        if (idGenerator == null) {
            throw new IllegalArgumentException(
                    "Id Generator should not be null");
        }

        if (storeFactory == null) {
            throw new IllegalArgumentException(
                    "StoreFactory should not be null");
        }

        this.idGenerator = idGenerator;
        this.storeFactory = storeFactory;

    }

    /**
     * Creates a new default (in memory) Store implementation.
     *
     * @param storeName The name of the actual data store object.
     * @param modelClass The class for the Store
     * 
     * @return a default in MemoryStore 
     */
    public Store store(String storeName, Class modelClass) {
        return store(storeName, new StoreConfig(modelClass));
    }
    /**
     * Creates a new Store implementation. The actual type is determined by the
     * type argument.
     *
     * @param storeName The name of the actual data store object.
     * @param config    The config object used to build the store
     * 
     * @return a store configured by the config param
     */
    public Store store(String storeName, StoreConfig config) {
        Store store = storeFactory.createStore(config);
        stores.put(storeName, store);
        return store;
    }

    /**
     * Creates a new default encrypted (in memory) Store implementation.
     *
     * @param storeName  The name of the actual data store object.
     * @param passphrase The passphrase used to create a KeyStore
     * @param modelClass The model class will be encrypted
     * 
     * @return a default encrypted in memory store
     * 
     * @throws java.security.spec.InvalidKeySpecException if the key spec is invalid
     *
     */
    public Store encryptedStore(String storeName, String passphrase, Class modelClass) throws InvalidKeySpecException {
        StoreConfig config = new StoreConfig(modelClass);
        config.setType(StoreTypes.ENCRYPTED_MEMORY);
        return encryptedStore(storeName, config, passphrase);
    }

    /**
     * Creates a new Store implementation. The actual type is determined by the
     * type argument.
     *
     * @param storeName  The name of the actual data store object.
     * @param config     The config object used to build the store
     * @param passphrase The passphrase used to create a KeyStore
     *
     * @return a default encrypted in memory store
     * @throws java.security.spec.InvalidKeySpecException if encryption is used, this method will
     * throw an exception if the keys provided on the config object are malformed.
     */
    public Store encryptedStore(String storeName, StoreConfig config, String passphrase)
            throws InvalidKeySpecException {
        config.setPassphrase(passphrase);
        
        Store store = storeFactory.createStore(config);
        stores.put(storeName, store);
        return store;
    }

    /**
     * Removes a Store implementation from the DataManager. The store to be
     * removed is determined by the storeName argument.
     *
     * @param storeName The name of the actual data store object.
     * 
     * @return the store removed or null
     */
    public Store remove(String storeName) {
        return stores.remove(storeName);
    }

    /**
     * Loads a given Store implementation, based on the given storeName argument.
     *
     * @param storeName The name of the actual data store object.
     * 
     * @return the store stored at storeName
     */
    public Store get(String storeName) {
        return stores.get(storeName);
    }
}
