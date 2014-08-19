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
package org.jboss.aerogear.android.impl.datamanager;

import android.content.Context;
import com.google.gson.GsonBuilder;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreFactory;
import org.jboss.aerogear.android.datamanager.StoreType;

public final class DefaultStoreFactory implements StoreFactory {

    @Override
    public Store createStore(StoreConfig config) {
        StoreType type = config.getType();
        IdGenerator idGenerator = config.getIdGenerator();
        Class klass = config.getKlass();
        Context context = config.getContext();
        GsonBuilder builder = config.getBuilder();
        String passphrase = config.getPassphrase();
        String name = config.getName();
        
        if (type.equals(StoreTypes.MEMORY)) {
            return new MemoryStorage(idGenerator);
        } else if (StoreTypes.SQL.equals(type)) {
            if (klass == null) {
                throw new IllegalArgumentException("StoreConfig.klass may not be null");
            }

            if (context == null) {
                throw new IllegalArgumentException("StoreConfig.context may not be null");
            }

            if (builder == null) {
                throw new IllegalArgumentException("StoreConfig.builder may not be null");
            }
            
            if (name == null) {
                throw new IllegalArgumentException("StoreConfig.name may not be null");
            }

            return new SQLStore(klass, context, builder, idGenerator, name);
        } else if (StoreTypes.ENCRYPTED_MEMORY.equals(type)) {
            return new EncryptedMemoryStore(idGenerator, passphrase, klass);
        } else if (StoreTypes.ENCRYPTED_SQL.equals(type)) {
            return new EncryptedSQLStore(klass, context, builder, idGenerator, passphrase, name);
        }
        throw new IllegalArgumentException("Type is not supported yet");
    }

}