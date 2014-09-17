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
import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.OnStoreCreatedListener;

public final class EncryptedSQLStoreConfiguration extends StoreConfiguration<EncryptedSQLStoreConfiguration>
        implements Config<EncryptedSQLStoreConfiguration> {

    private Class klass;
    private Context context;
    private GsonBuilder builder = new GsonBuilder();
    private IdGenerator idGenerator = new DefaultIdGenerator();
    private String passphrase;

    public EncryptedSQLStoreConfiguration setKlass(Class klass) {
        this.klass = klass;
        return this;
    }

    public EncryptedSQLStoreConfiguration setContext(Context context) {
        this.context = context;
        return this;
    }

    public EncryptedSQLStoreConfiguration setBuilder(GsonBuilder builder) {
        this.builder = builder;
        return this;
    }

    public EncryptedSQLStoreConfiguration setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    public EncryptedSQLStoreConfiguration setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        return this;
    }

    public <T> EncryptedSQLStore<T> createEncryptedSQLStore() {
        EncryptedSQLStore<T> encryptedSQLStore = new EncryptedSQLStore<T>(klass, context, builder, idGenerator, passphrase);

        for (OnStoreCreatedListener listener : getOnStoreCreatedListeners()) {
            listener.onStoreCreated(this, encryptedSQLStore);
        }

        return encryptedSQLStore;
    }

}
