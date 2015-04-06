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
package org.jboss.aerogear.android.store.test.sql;

import android.content.Context;
import android.test.RenamingDelegatingContext;
import junit.framework.Assert;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.core.RecordId;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.StoreNotOpenException;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.jboss.aerogear.android.store.test.helper.Data;
import org.jboss.aerogear.android.store.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;
import org.jboss.aerogear.android.store.test.MainActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SQLStoreTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public SQLStoreTest() {
        super(MainActivity.class);
    }

    private Context context;
    private SQLStore<Data> store;
    private SQLStore<TrivialNestedClass> nestedStore;
    private SQLStore<TrivialNestedClassWithCollection> nestedWithCollectionStore;

    @Override
    protected void runTest() throws Throwable {
        if (!System.getProperty("os.name").toLowerCase().startsWith("mac os x")
                || !System.getProperty("java.version").startsWith("1.7.0")) {
            super.runTest();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.context = new RenamingDelegatingContext(getActivity(), UUID.randomUUID().toString());

        this.store = (SQLStore) DataManager
                .config("store", SQLStoreConfiguration.class)
                .forClass(Data.class)
                .withContext(context)
                .store();


        this.nestedStore = (SQLStore) DataManager
                .config("trivialNestedClass", SQLStoreConfiguration.class)
                .forClass(TrivialNestedClass.class)
                .withContext(context)
                .store();

        this.nestedWithCollectionStore = (SQLStore) DataManager
                .config("trivialNestedClassWithCollection", SQLStoreConfiguration.class)
                .forClass(TrivialNestedClassWithCollection.class)
                .withContext(context)
                .store();
    }

    public void testCreateSQLStoreWithoutKlass() {

        try {
            Store<Data> store1 = DataManager.config("store1", SQLStoreConfiguration.class)
                    .withContext(context)
                    .store();

            Data data = new Data(10, "name", "description");
            store1.save(data);

            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            //success
        }

    }

    public void testCreateSQLStoreWithoutContext() {

        try {
            Store<Data> store2 = DataManager.config("store2", SQLStoreConfiguration.class)
                    .forClass(Data.class)
                    .store();

            Data data = new Data(10, "name", "description");
            store2.save(data);

            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            //success
        }

    }

    public void testCreateSQLStoreWithoutContextAndKlass() {

        try {
            Store<Data> store3 = DataManager.config("store3", SQLStoreConfiguration.class).store();

            Data data = new Data(10, "name", "description");
            store3.save(data);

            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            //success
        }

    }

    public void testSave() throws InterruptedException {
        Data data = new Data(10, "name", "description");
        data.setEnable(true);
        saveData(10, "name", "description", true);
        Data readData = store.read(10);
        assertEquals(data, readData);
    }

    public void testReset() throws InterruptedException {
        saveData(10, "name", "description");
        store.reset();
        Data readData = store.read(10);
        assertNull(readData);
    }

    public void testIsEmpty() throws InterruptedException {
        store.openSync();
        assertTrue(store.isEmpty());
    }

    public void testIsNotEmpty() throws InterruptedException {
        saveData(10, "name", "description");
        assertFalse(store.isEmpty());
    }

    public void testReadAll() throws InterruptedException, JSONException {
        loadBulkData();
        List<Data> allData = new ArrayList<Data>(store.readAll());
        Collections.sort(allData);
        assertEquals(6, allData.size());
        assertEquals("name", allData.get(0).getName());
        assertEquals("name2", allData.get(5).getName());

    }

    public void testRemove() throws InterruptedException, JSONException {
        loadBulkData();
        store.remove(1);

        List<Data> allData = new ArrayList<Data>(store.readAll());
        Collections.sort(allData);
        assertEquals(5, allData.size());
        assertEquals(2l, (long) allData.get(0).getId());
        assertEquals("name2", allData.get(4).getName());

    }

    public void testFilter() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<Data> result;

        loadBulkData();

        result = store.readWithFilter(null);
        assertEquals(6, result.size());

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("name", "name2");
        filter.setWhere(where);
        result = store.readWithFilter(filter);
        assertEquals(3, result.size());

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("name", "name2");
        where.put("description", "description");
        filter.setWhere(where);
        result = store.readWithFilter(filter);
        assertEquals(2, result.size());

    }

    public void testNestedSaveAndFilter() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<TrivialNestedClass> result;

        Data data = new Data(10, "name", "description");

        TrivialNestedClass newNested = new TrivialNestedClass();
        newNested.setId(1);
        newNested.setText("nestedText");
        newNested.setData(data);

        nestedStore.openSync();
        nestedStore.save(newNested);

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("text", "nestedText");
        JSONObject dataFilter = new JSONObject();
        dataFilter.put("id", 10);
        where.put("data", dataFilter);
        filter.setWhere(where);
        result = nestedStore.readWithFilter(filter);
        assertEquals(1, result.size());
        TrivialNestedClass nestedResult = result.get(0);
        assertEquals("name", nestedResult.data.getName());

    }

    public void testNestedListSaveAndFilter() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<TrivialNestedClassWithCollection> result;

        List<Data> data = new ArrayList<Data>();
        data.add(new Data(10, "name", "description"));
        data.add(new Data(30, "name", "description"));

        TrivialNestedClassWithCollection newNested = new TrivialNestedClassWithCollection();
        newNested.setId(1);
        newNested.setText("nestedText");
        newNested.setData(data);

        nestedWithCollectionStore.openSync();
        nestedWithCollectionStore.save(newNested);

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("text", "nestedText");
        where.put("id", 1);

        filter.setWhere(where);
        result = nestedWithCollectionStore.readWithFilter(filter);
        assertEquals(1, result.size());
        TrivialNestedClassWithCollection nestedResult = result.get(0);
        assertEquals((Integer) 10, nestedResult.data.get(0).getId());
        assertEquals((Integer) 30, nestedResult.data.get(1).getId());

    }

    
    public void testNestedListSaveAndFilterOnTestedCollection() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<TrivialNestedClassWithCollection> result;

        List<Data> data = new ArrayList<Data>();
        data.add(new Data(10, "name1", "description"));
        data.add(new Data(30, "name2", "description"));

        TrivialNestedClassWithCollection newNested = new TrivialNestedClassWithCollection();
        newNested.setId(1);
        newNested.setText("nestedText1");
        newNested.setData(data);

        nestedWithCollectionStore.openSync();
        nestedWithCollectionStore.save(newNested);

        
        data = new ArrayList<Data>();
        data.add(new Data(10, "name3", "description"));
        data.add(new Data(30, "name4", "description"));

        newNested = new TrivialNestedClassWithCollection();
        newNested.setId(2);
        newNested.setText("nestedText2");
        newNested.setData(data);

        nestedWithCollectionStore.save(newNested);

        
        filter = new ReadFilter();
        where = new JSONObject();
        where.put("data[%].name", "name2");
        

        filter.setWhere(where);
        result = nestedWithCollectionStore.readWithFilter(filter);
        assertEquals(1, result.size());
        TrivialNestedClassWithCollection nestedResult = result.get(0);
        assertEquals((Integer) 10, nestedResult.data.get(0).getId());
        assertEquals((Integer) 30, nestedResult.data.get(1).getId());

    }

    
    public void testSuccessCallback() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        store.open(new Callback<SQLStore<Data>>() {
            @Override
            public void onSuccess(SQLStore<Data> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertFalse("OnSuccess should be called exactly once!", latch.await(5, TimeUnit.SECONDS));

    }

    private void saveData(Integer id, String name, String desc) throws InterruptedException {
        store.openSync();
        store.save(new Data(id, name, desc));
    }

    private void saveData(Integer id, String name, String desc, boolean enable) throws InterruptedException {
        store.openSync();
        store.save(new Data(id, name, desc, enable));
    }

    public void testSaveListOfBoringData() throws InterruptedException {
        SQLStore<ListWithId> longStore = new SQLStore<ListWithId>(ListWithId.class, context);
        longStore.openSync();
        ListWithId<Long> longList = new ListWithId<Long>(100);

        longList.setId(1);

        for (long i = 0; i < 100; i++) {
            longList.data.add(i);
        }
        longStore.save(longList);
        assertEquals(100, longStore.readAll().iterator().next().data.size());

    }

    public void testReadAllWithClosedStore() {
        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.readAll();
            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    public void testReadWithClosedStore() {
        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.read(Long.valueOf("1"));
            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    public void testReadWithFilterWithClosedStore() {
        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.readWithFilter(new ReadFilter());
            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    public void testSaveWithClosedStore() {
        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.save(new Data("AeroGear", "The best framework for mobile development."));
            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    public void testRemoveWithClosedStore() {
        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.remove(Long.valueOf("1"));
            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    public void testResetWithClosedStore() {
        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.reset();
            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    public void testIsEmptyWithClosedStore() {
        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.isEmpty();
            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    public void testSaveCollection() {
        List<Data> items = new ArrayList<Data>();
        items.add(new Data(1, "Item 1", "This is the item 1"));
        items.add(new Data(2, "Item 2", "This is the item 2"));
        items.add(new Data(3, "Item 3", "This is the item 3"));
        items.add(new Data(4, "Item 4", "This is the item 4"));
        items.add(new Data(5, "Item 5", "This is the item 5"));

        SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
        store.openSync();
        store.save(items);

        Assert.assertEquals("Should have " + items.size() + " items", items.size(), store.readAll().size());
    }

    public void testSaveCollectionWithClosedStore() {
        List<Data> items = new ArrayList<Data>();
        items.add(new Data(1, "Item 1", "This is the item 1"));
        items.add(new Data(2, "Item 2", "This is the item 2"));
        items.add(new Data(3, "Item 3", "This is the item 3"));
        items.add(new Data(4, "Item 4", "This is the item 4"));
        items.add(new Data(5, "Item 5", "This is the item 5"));

        try {
            SQLStore<Data> store = new SQLStore<Data>(Data.class, context);
            store.save(items);

            Assert.fail("Should have thrown StoreNotOpenException");
        } catch (StoreNotOpenException e) {
            // Sucess
        }
    }

    private void loadBulkData() throws InterruptedException {
        saveData(1, "name", "description");
        saveData(2, "name", "description");
        saveData(3, "name2", "description");
        saveData(4, "name2", "description");
        saveData(5, "name", "description2");
        saveData(6, "name2", "description2");
    }

    public static final class TrivialNestedClass {

        @RecordId
        private Integer id;
        private String text;
        private Data data;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

    }

    public static final class TrivialNestedClassWithCollection {

        @RecordId
        private Integer id;
        private String text;
        private List<Data> data;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Data> getData() {
            return data;
        }

        public void setData(List<Data> data) {
            this.data = data;
        }

    }

    public static final class ListWithId<T> {

        @RecordId
        private Integer id;

        public final List<T> data;

        public ListWithId(int size) {
            data = new ArrayList<T>(size);
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

    }

}
