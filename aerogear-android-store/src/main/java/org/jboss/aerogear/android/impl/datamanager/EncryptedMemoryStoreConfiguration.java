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

import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;

public final class EncryptedMemoryStoreConfiguration extends StoreConfiguration<EncryptedMemoryStoreConfiguration>
        implements Config<EncryptedMemoryStoreConfiguration> {

    private IdGenerator idGenerator = new DefaultIdGenerator();
    private String passphrase;
    private Class klass;

    public EncryptedMemoryStoreConfiguration withIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    public EncryptedMemoryStoreConfiguration usingPassphrase(String passphrase) {
        this.passphrase = passphrase;
        return this;
    }

    public EncryptedMemoryStoreConfiguration forClass(Class klass) {
        this.klass = klass;
        return this;
    }


    @Override
    protected Store buildStore() {
        if((klass == null) || (passphrase == null)) {
            throw new IllegalStateException("Klass and Passphrase are mandatory");
        }

        return new EncryptedMemoryStore(idGenerator, passphrase, klass);
    }

}
