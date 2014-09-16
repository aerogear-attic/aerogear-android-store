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

public final class SQLStoreConfig extends StoreConfig2<SQLStoreConfig> implements Config<SQLStoreConfig> {

    private Class klass;
    private Context context;
    private GsonBuilder builder = new GsonBuilder();
    private IdGenerator idGenerator = new DefaultIdGenerator();

    public SQLStoreConfig setKlass(Class klass) {
        this.klass = klass;
        return this;
    }

    public SQLStoreConfig setContext(Context context) {
        this.context = context;
        return this;
    }

    public SQLStoreConfig setBuilder(GsonBuilder builder) {
        this.builder = builder;
        return this;
    }

    public SQLStoreConfig setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    public <T> SQLStore<T> createSQLStore() {
        SQLStore<T> sqlStore = new SQLStore<T>(klass, context, builder, idGenerator);

        for (OnStoreCreatedListener listener : getOnStoreCreatedListeners()) {
            listener.onStoreCreated(this, sqlStore);
        }

        return sqlStore;
    }

}