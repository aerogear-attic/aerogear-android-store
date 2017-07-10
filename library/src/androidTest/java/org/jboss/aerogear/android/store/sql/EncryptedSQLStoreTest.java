/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.store.sql;

import android.content.Context;
import android.os.StrictMode;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.GsonBuilder;

import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.generator.DefaultIdGenerator;
import org.jboss.aerogear.android.store.helper.Data;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EncryptedSQLStoreTest {

    private static final StrictMode.VmPolicy DEFAULT_VM_POLICY = StrictMode.getVmPolicy();

    private static final StrictMode.VmPolicy STRICT_VM_POLICY = new StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectLeakedClosableObjects()
            .penaltyLog()
            .penaltyDeath()
            .build();

    private Context context;
    private EncryptedSQLStore<Data> store;

    @Before
    public void setUp() throws Exception {

        StrictMode.setVmPolicy(STRICT_VM_POLICY);

        this.context = getContext();

        store = (EncryptedSQLStore<Data>) DataManager.config("myTestStore", EncryptedSQLStoreConfiguration.class)
                .withContext(context)
                .usingPassphrase("AeroGear")
                .store(Data.class);

    }

    @After
    public void closeAll() {
        this.store.close();
        StrictMode.setVmPolicy(DEFAULT_VM_POLICY);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutKlass() {

        Store<Data> store1 = DataManager.config("store1", EncryptedSQLStoreConfiguration.class)
                .withContext(context)
                .usingPassphrase("AeroGear")
                .store(null);

        Data data = new Data(10, "name", "description");
        store1.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutContext() {

        Store<Data> store2 = DataManager.config("store2", EncryptedSQLStoreConfiguration.class)
                .usingPassphrase("AeroGear")
                .store(Data.class);

        Data data = new Data(10, "name", "description");
        store2.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutPassphrase() {

        Store<Data> store3 = DataManager.config("store3", EncryptedSQLStoreConfiguration.class)
                .withContext(context)
                .store(Data.class);

        Data data = new Data(10, "name", "description");
        store3.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutContextAndKlassAndPassphrase() {
        Store<Data> store4 = DataManager.config("store3", EncryptedSQLStoreConfiguration.class).store(null);

        Data data = new Data(10, "name", "description");
        store4.save(data);

    }

    @Test
    public void testReadAll() {
        for (int i = 1; i <= 10; i++) {
            Data data = new Data(i, "name " + i, "description " + i);
            store.save(data);
        }

        Assert.assertEquals("Store should have 10 elements", 10, store.readAll().size());
    }

    @Test
    public void testRead() {
        Data data = new Data(10, "name", "description");
        store.save(data);

        Data readData = store.read(10);
        Assert.assertEquals(data, readData);
        assertTrue("Store can not be empty", !store.isEmpty());
    }

    @Test
    public void testSave() {
        Data data = new Data(10, "name", "description");
        store.save(data);

        assertFalse("Store can not be empty", store.isEmpty());
        Assert.assertEquals("Store should have 1 elements", 1, store.readAll().size());
    }

    @Test
    public void testReset() {
        Data data = new Data(10, "name", "description");
        store.save(data);
        store.reset();

        assertTrue("Store can empty", store.isEmpty());
    }

    @Test
    public void testRemove() {
        for (int i = 1; i <= 10; i++) {
            Data data = new Data(i, "name " + i, "description " + i);
            store.save(data);
        }

        store.remove(1);
        Assert.assertEquals("Store should have 9 elements", 9, store.readAll().size());
    }

    @Test
    public void testIsEmpty() {
        assertTrue("Store can empty", store.isEmpty());
    }

    @Test
    public void testIsNotEmpty() {
        Data data = new Data(10, "name", "description");
        store.save(data);

        assertFalse("Store can not be empty", store.isEmpty());
    }

    @Test
    public void testSaveCollection() {
        List<Data> items = new ArrayList<Data>();
        items.add(new Data(1, "Item 1", "This is the item 1"));
        items.add(new Data(2, "Item 2", "This is the item 2"));
        items.add(new Data(3, "Item 3", "This is the item 3"));
        items.add(new Data(4, "Item 4", "This is the item 4"));
        items.add(new Data(5, "Item 5", "This is the item 5"));

        EncryptedSQLStore<Data> store = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");

        store.save(items);

        Assert.assertEquals("Should have " + items.size() + " items", items.size(), store.readAll().size());
        store.close();
    }
}
