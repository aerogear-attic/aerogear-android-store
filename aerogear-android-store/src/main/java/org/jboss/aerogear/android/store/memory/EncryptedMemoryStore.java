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

import android.content.Context;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.security.EncryptionService;
import org.jboss.aerogear.android.security.InvalidKeyException;
import org.jboss.aerogear.android.security.keystore.KeyStoreBasedEncryptionConfiguration;
import org.jboss.aerogear.android.security.util.CryptoEntityUtil;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.generator.IdGenerator;
import org.jboss.aerogear.crypto.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EncryptedMemoryStore<T> implements Store<T> {

    private final MemoryStore<byte[]> memoryStore;
    private final CryptoEntityUtil<T> cryptoEntityUtil;

    public EncryptedMemoryStore(Context context, IdGenerator idGenerator, String password, Class<T> modelClass) {

        memoryStore = new MemoryStore<byte[]>(idGenerator);

        byte[] iv = RandomUtils.randomBytes();

        EncryptionService encryptionService = org.jboss.aerogear.android.security.SecurityManager
                .config(modelClass.getName(), KeyStoreBasedEncryptionConfiguration.class)
                .setContext(context)
                .setAlias(modelClass.getName())
                .setKeyStoreFile(modelClass.getName())
                .setPassword(password)
                .asService();

        cryptoEntityUtil = new CryptoEntityUtil<T>(encryptionService, iv, modelClass);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> readAll() throws InvalidKeyException {
        ArrayList<T> dataList = new ArrayList<T>();

        Collection<byte[]> encryptedCollection = memoryStore.readAll();
        for (byte[] encryptedData : encryptedCollection) {
            T decryptedData = cryptoEntityUtil.decrypt(encryptedData);
            dataList.add(decryptedData);
        }

        return dataList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T read(Serializable id) throws InvalidKeyException {
        byte[] encryptedItem = memoryStore.read(id);
        if (encryptedItem == null) {
            return null;
        } else {
            return cryptoEntityUtil.decrypt(encryptedItem);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> readWithFilter(ReadFilter filter) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(T item) {
        Serializable idValue = memoryStore.getOrGenerateIdValue(item);
        memoryStore.save(idValue, cryptoEntityUtil.encrypt(item));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Collection<T> items) {
        for (T item : items) {
            save(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        memoryStore.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Serializable id) {
        memoryStore.remove(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return memoryStore.isEmpty();
    }

}
