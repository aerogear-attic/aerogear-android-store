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
import android.test.RenamingDelegatingContext;
import junit.framework.Assert;
import org.jboss.aerogear.android.DataManager;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.store.test.MainActivity;

import java.util.UUID;

public class EncryptedSQLStoreTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public EncryptedSQLStoreTest() {
        super(MainActivity.class);
    }

    private Context context;
    private EncryptedSQLStore<Data> store;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.context = new RenamingDelegatingContext(getActivity(), UUID.randomUUID().toString());

        store = (EncryptedSQLStore<Data>) DataManager.config("myTestStore", EncryptedSQLStoreConfiguration.class)
                .withContext(context)
                .usingPassphrase("AeroGear")
                .forClass(Data.class)
                .store();

        store.openSync();

    }

    public void testCreateSQLStoreWithoutKlass() {

        try {
            EncryptedSQLStore<Data> store1 = (EncryptedSQLStore<Data>)
                    DataManager.config("store1", EncryptedSQLStoreConfiguration.class)
                    .withContext(context)
                    .usingPassphrase("AeroGear")
                    .store();
            store1.openSync();


            Data data = new Data(10, "name", "description");
            store1.save(data);

            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            //success
        }

    }

    public void testCreateSQLStoreWithoutContext() {

        try {
            EncryptedSQLStore<Data> store2 = (EncryptedSQLStore<Data>)
                    DataManager.config("store2", EncryptedSQLStoreConfiguration.class)
                    .forClass(Data.class)
                    .usingPassphrase("AeroGear")
                    .store();
            store2.openSync();

            Data data = new Data(10, "name", "description");
            store2.save(data);

            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            //success
        }

    }

    public void testCreateSQLStoreWithoutPassphrase() {

        try {
            EncryptedSQLStore<Data> store3 = (EncryptedSQLStore<Data>)
                    DataManager.config("store3", EncryptedSQLStoreConfiguration.class)
                    .withContext(context)
                    .forClass(String.class)
                    .store();
            store3.openSync();

            Data data = new Data(10, "name", "description");
            store3.save(data);

            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            //success
        }

    }

    public void testCreateSQLStoreWithoutContextAndKlassAndPassphrase() {

        try {
            EncryptedSQLStore<Data> store4 = (EncryptedSQLStore<Data>)
                    DataManager.config("store3", EncryptedSQLStoreConfiguration.class).store();
            store4.openSync();

            Data data = new Data(10, "name", "description");
            store4.save(data);

            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            //success
        }

    }

    public void testReadAll() {
        for (int i = 1; i <= 10; i++) {
            Data data = new Data(i, "name " + i, "description " + i);
            store.save(data);
        }

        assertEquals("Store should have 10 elements", 10, store.readAll().size());
    }

    public void testRead() {
        Data data = new Data(10, "name", "description");
        store.save(data);

        Data readData = store.read(10);
        assertEquals(data, readData);
        assertTrue("Store can not be empty", !store.isEmpty());
    }

    public void testSave() {
        Data data = new Data(10, "name", "description");
        store.save(data);

        assertFalse("Store can not be empty", store.isEmpty());
        assertEquals("Store should have 1 elements", 1, store.readAll().size());
    }

    public void testReset() {
        Data data = new Data(10, "name", "description");
        store.save(data);
        store.reset();

        assertTrue("Store can empty", store.isEmpty());
    }

    public void testRemove() {
        for (int i = 1; i <= 10; i++) {
            Data data = new Data(i, "name " + i, "description " + i);
            store.save(data);
        }

        store.remove(1);

        assertEquals("Store should have 9 elements", 9, store.readAll().size());
    }

    public void testIsEmpty() {
        assertTrue("Store can empty", store.isEmpty());
    }

    public void testIsNotEmpty() {
        Data data = new Data(10, "name", "description");
        store.save(data);

        assertFalse("Store can not be empty", store.isEmpty());
    }

}
