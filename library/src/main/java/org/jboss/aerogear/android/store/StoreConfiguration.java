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

import org.jboss.aerogear.android.core.Config;

import java.util.Collection;
import java.util.HashSet;

public abstract class StoreConfiguration<CONFIGURATION extends StoreConfiguration<CONFIGURATION>>
        implements Config<CONFIGURATION> {

    private String name;
    private Collection<OnStoreCreatedListener> listeners;

    public StoreConfiguration() {
        listeners = new HashSet<OnStoreCreatedListener>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CONFIGURATION setName(String name) {
        this.name = name;
        return (CONFIGURATION) this;
    }

    public Collection<OnStoreCreatedListener> getOnStoreCreatedListeners() {
        return listeners;
    }

    public CONFIGURATION addOnStoreCreatedListener(OnStoreCreatedListener listener) {
        this.listeners.add(listener);
        return (CONFIGURATION) this;
    }

    public CONFIGURATION setOnStoreCreatedListeners(Collection<OnStoreCreatedListener> listeners) {
        listeners.addAll(listeners);
        return (CONFIGURATION) this;
    }

    public final <TYPE> Store<TYPE> store(Class<TYPE> klass) {
        Store<TYPE> store = buildStore(klass);
        for (OnStoreCreatedListener listener : getOnStoreCreatedListeners()) {
            listener.onStoreCreated(this, store);
        }
        return store;
    }

    protected abstract <TYPE> Store<TYPE> buildStore(Class<TYPE> klass);

}
