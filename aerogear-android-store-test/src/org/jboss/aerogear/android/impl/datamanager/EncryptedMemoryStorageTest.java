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

import org.jboss.aerogear.android.DataManager2;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.store.MainActivity;
import org.jboss.aerogear.android.store.impl.util.PatchedActivityInstrumentationTestCase;

import java.util.Collection;

import static org.jboss.aerogear.android.impl.datamanager.StoreTypes.ENCRYPTED_MEMORY;

public class EncryptedMemoryStorageTest  extends PatchedActivityInstrumentationTestCase<MainActivity> {

    private EncryptedMemoryStore<Data> store;

    public EncryptedMemoryStorageTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        StubIdGenerator stubIdGenerator = new StubIdGenerator();
        String passphrase = "Lorem Ipsum";
        Class<Data> dataModel = Data.class;

        store = DataManager2
                .config("testMemoryStore", EncryptedMemoryStoreConfig.class)
                .setIdGenerator(stubIdGenerator)
                .setPassphrase(passphrase)
                .setKlass(dataModel)
                .createEncryptedMemoryStore();
    }

    
    public void testStoreType() {
        assertEquals("verifying the type", ENCRYPTED_MEMORY, store.getType());
    }

    public void testReadAll() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Collection<Data> datas = store.readAll();
        assertNotNull("datas could not be null", datas);
        assertEquals("datas should 2 data", 2, datas.size());
    }

    
    public void testReadWithFilter() {
        try {
            Collection<Data> datas = store.readWithFilter(new ReadFilter());
        } catch (UnsupportedOperationException ignore) {
            return;
        }
        fail("Should raise an exception");
    }

    public void testRead() {
        store.save(new Data("foo", "desc of foo"));
        Data data = store.read(1);
        assertNotNull("data could not be null", data);
    }

    public void testSaveNoExistRecord() {
        Data data = new Data("foo", "desc of foo");
        store.save(data);
        assertEquals(Integer.valueOf(1), data.getId());
    }

    public void testSaveExistRecord() {
        Data data = new Data("foo", "desc of foo");
        store.save(data);

        // Simulate sending the same id but not necessarily the same instance
        data = new Data(1, "bar", "desc of bar");
        store.save(data);

        data = store.read(1);

        assertEquals(Integer.valueOf(1), data.getId());
        assertEquals("bar", data.getName());
        assertEquals("desc of bar", data.getDescription());
    }

    public void testReset() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Data foo = store.read(1);
        assertNotNull("foo could not be null", foo);

        Data bar = store.read(2);
        assertNotNull("bar could not be null", bar);

        store.reset();

        foo = store.read(1);
        assertNull("foo should be null", foo);

        bar = store.read(2);
        assertNull("bar should be null", bar);
    }

    public void testRemove() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Data foo = store.read(1);
        assertNotNull("foo could not be null", foo);

        Data bar = store.read(2);
        assertNotNull("bar could not be null", bar);

        store.remove(2);

        foo = store.read(1);
        assertNotNull("foo could not be null", foo);

        bar = store.read(2);
        assertNull("bar should be null", bar);
    }

    public void testIsEmpty() {
        assertTrue("should be empty", store.isEmpty());
    }

    public void testIsNotEmpty() {
        store.save(new Data("foo", "desc of foo"));
        assertFalse("should not be empty", store.isEmpty());
    }

}
