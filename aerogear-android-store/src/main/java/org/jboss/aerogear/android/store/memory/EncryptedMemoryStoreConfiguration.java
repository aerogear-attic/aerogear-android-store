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
import org.jboss.aerogear.android.core.Config;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.StoreConfiguration;
import org.jboss.aerogear.android.store.generator.DefaultIdGenerator;
import org.jboss.aerogear.android.store.generator.IdGenerator;

public final class EncryptedMemoryStoreConfiguration extends StoreConfiguration<EncryptedMemoryStoreConfiguration>
        implements Config<EncryptedMemoryStoreConfiguration> {

    private Context context;
    private IdGenerator idGenerator = new DefaultIdGenerator();
    private String password;

    public EncryptedMemoryStoreConfiguration withContext(Context context) {
        this.context = context;
        return this;
    }

    public EncryptedMemoryStoreConfiguration withIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    public EncryptedMemoryStoreConfiguration usingPassword(String passphrase) {
        this.password = passphrase;
        return this;
    }


    @Override
    protected <TYPE> Store<TYPE> buildStore(Class<TYPE> klass) {
        if ((context == null) || (klass == null) || (password == null)) {
            throw new IllegalStateException("Context, Klass and Passphrase are mandatory");
        }

        return new EncryptedMemoryStore<>(context, idGenerator, password, klass);
    }

}
