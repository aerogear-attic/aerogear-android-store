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

import org.jboss.aerogear.android.core.Config;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.generator.DefaultIdGenerator;
import org.jboss.aerogear.android.store.generator.IdGenerator;
import org.jboss.aerogear.android.store.StoreConfiguration;

public final class MemoryStoreConfiguration extends StoreConfiguration<MemoryStoreConfiguration>
        implements Config<MemoryStoreConfiguration> {

    private IdGenerator idGenerator = new DefaultIdGenerator();

    public MemoryStoreConfiguration withIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    @Override
    protected <TYPE> Store<TYPE> buildStore(Class<TYPE> klass) {
        return new MemoryStore(idGenerator);
    }

}
