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

/**
 * This class bundles up all of the possible variables which may be used to instantiate a {@link Store}
 */
public final class StoreConfig {

    /**
     * An Android Context, used by {@link SQLStore}
     */
    private Context context;

    /**
     * The Class of the store, should be the same as the parameterized class
     * of the Store.  Used by {@link SQLStore}
     */
    private final Class klass;

    /**
     * The type of Store this instance will build when consumed by a {@link StoreFactory}
     * Defaults to MEMORY.
     */
    private StoreType type = StoreTypes.MEMORY;

    /**
     * The builder to use to manage objects.  Used by {@link SQLStore}
     * Defaults to new GsonBuilder();
     */
    private GsonBuilder builder = new GsonBuilder();

    /**
     * The IdGenerator used by the Store.  Used by {@link SQLStore} and {@link MemoryStorage}.
     * Defaults to new DefaultIdGenerator();
     */
    private IdGenerator idGenerator = new DefaultIdGenerator();

    /**
     * The PrivateKey used to crypt/decrypt data
     */
    private String passphrase;
    
    /**
     * The name of the store, defaults to klass.getSimpleName
     */ 
    private String name;

    public StoreConfig(Class klass) {
        this.klass = klass;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Class getKlass() {
        return klass;
    }

    public StoreType getType() {
        return type;
    }

    public void setType(StoreType type) {
        this.type = type;
    }

    public GsonBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(GsonBuilder builder) {
        this.builder = builder;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getName() {
        if ((name == null || name.isEmpty()) && klass != null) {
            return klass.getSimpleName();
        } else {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }
    
}