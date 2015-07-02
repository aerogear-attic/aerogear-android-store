/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.store.test.sql;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.test.helper.Data;
import org.jboss.aerogear.android.store.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.store.StoreNotOpenException;
import org.jboss.aerogear.android.store.generator.DefaultIdGenerator;
import org.jboss.aerogear.android.store.sql.EncryptedSQLStore;
import org.jboss.aerogear.android.store.sql.EncryptedSQLStoreConfiguration;
import org.jboss.aerogear.android.store.test.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EncryptedSQLStoreTest extends PatchedActivityInstrumentationTestCase {

    public EncryptedSQLStoreTest() {
        super(MainActivity.class);
    }

    private Context context;
    private EncryptedSQLStore<Data> store;

    @Before
    public void setUp() throws Exception {

        this.context = new RenamingDelegatingContext(getActivity(), UUID.randomUUID().toString());

        store = (EncryptedSQLStore<Data>) DataManager.config("myTestStore", EncryptedSQLStoreConfiguration.class)
                .withContext(context)
                .usingPassphrase("AeroGear")
                .forClass(Data.class)
                .store();

        store.openSync();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutKlass() {

        EncryptedSQLStore<Data> store1 = (EncryptedSQLStore<Data>) DataManager.config("store1", EncryptedSQLStoreConfiguration.class)
                .withContext(context)
                .usingPassphrase("AeroGear")
                .store();
        store1.openSync();

        Data data = new Data(10, "name", "description");
        store1.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutContext() {

        EncryptedSQLStore<Data> store2 = (EncryptedSQLStore<Data>) DataManager.config("store2", EncryptedSQLStoreConfiguration.class)
                .forClass(Data.class)
                .usingPassphrase("AeroGear")
                .store();
        store2.openSync();

        Data data = new Data(10, "name", "description");
        store2.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutPassphrase() {

        EncryptedSQLStore<Data> store3 = (EncryptedSQLStore<Data>) DataManager.config("store3", EncryptedSQLStoreConfiguration.class)
                .withContext(context)
                .forClass(String.class)
                .store();
        store3.openSync();

        Data data = new Data(10, "name", "description");
        store3.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutContextAndKlassAndPassphrase() {
        EncryptedSQLStore<Data> store4 = (EncryptedSQLStore<Data>) DataManager.config("store3", EncryptedSQLStoreConfiguration.class).store();
        store4.openSync();

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

    @Test(expected = StoreNotOpenException.class)
    public void testReadAllWithClosedStore() {
        EncryptedSQLStore<Data> closedStore = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");
        closedStore.readAll();

    }

    @Test(expected = StoreNotOpenException.class)
    public void testReadWithClosedStore() {

        EncryptedSQLStore<Data> closedStore = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");
        closedStore.read(Long.valueOf("1"));

    }

    @Test(expected = StoreNotOpenException.class)
    public void testSaveWithClosedStore() {

        EncryptedSQLStore<Data> closedStore = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");
        closedStore.save(new Data("AeroGear", "The best framework for mobile development."));

    }

    @Test(expected = StoreNotOpenException.class)
    public void testRemoveWithClosedStore() {

        EncryptedSQLStore<Data> closedStore = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");
        closedStore.remove(Long.valueOf("1"));

    }

    @Test(expected = StoreNotOpenException.class)
    public void testResetWithClosedStore() {
        EncryptedSQLStore<Data> closedStore = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");
        closedStore.reset();

    }

    @Test(expected = StoreNotOpenException.class)
    public void testIsEmptyWithClosedStore() {

        EncryptedSQLStore<Data> closedStore = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");
        closedStore.isEmpty();

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
        store.openSync();
        store.save(items);

        Assert.assertEquals("Should have " + items.size() + " items", items.size(), store.readAll().size());
    }

    @Test(expected = StoreNotOpenException.class)
    public void testSaveCollectionWithClosedStore() {
        List<Data> items = new ArrayList<Data>();
        items.add(new Data(1, "Item 1", "This is the item 1"));
        items.add(new Data(2, "Item 2", "This is the item 2"));
        items.add(new Data(3, "Item 3", "This is the item 3"));
        items.add(new Data(4, "Item 4", "This is the item 4"));
        items.add(new Data(5, "Item 5", "This is the item 5"));

        EncryptedSQLStore<Data> store = new EncryptedSQLStore<Data>(
                Data.class, context, new GsonBuilder(), new DefaultIdGenerator(), "AeroGear");
        store.save(items);

    }

}
