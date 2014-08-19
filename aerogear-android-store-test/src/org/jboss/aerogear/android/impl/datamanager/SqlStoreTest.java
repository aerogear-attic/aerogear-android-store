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
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.RecordId;
import org.jboss.aerogear.android.impl.helper.Data;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
public class SqlStoreTest {

    private Context context;
    private SQLStore<Data> store;
    private SQLStore<TrivialNestedClass> nestedStore;
    private SQLStore<TrivialNestedClassWithCollection> nestedWithCollectionStore;

    @Before
    public void setUp() {
        // Let's not run this test on Mac OS X with Java 1.7 until SQLite is compatible with that configuration
        Assume.assumeTrue(!System.getProperty("os.name").toLowerCase().startsWith("mac os x")
                || !System.getProperty("java.version").startsWith("1.7.0"));

        this.context = Robolectric.application.getApplicationContext();
        this.store = new SQLStore<Data>(Data.class, context);
        this.nestedStore = new SQLStore<TrivialNestedClass>(TrivialNestedClass.class, context);
        this.nestedWithCollectionStore = new SQLStore<TrivialNestedClassWithCollection>(TrivialNestedClassWithCollection.class, context);
    }

    @Test
    public void testSave() throws InterruptedException {

        Data data = new Data(10, "name", "description");
        data.setEnable(true);
        saveData(10, "name", "description", true);
        Data readData = store.read(10);
        Assert.assertEquals(data, readData);
    }

    @Test
    public void testReset() throws InterruptedException {
        saveData(10, "name", "description");
        store.reset();
        Data readData = store.read(10);
        Assert.assertNull(readData);
    }

    @Test
    public void testIsEmpty() throws InterruptedException {
        open(store);
        Assert.assertTrue(store.isEmpty());
    }

    @Test
    public void testIsNotEmpty() throws InterruptedException {
        saveData(10, "name", "description");
        Assert.assertFalse(store.isEmpty());
    }

    @Test
    public void testReadAll() throws InterruptedException, JSONException {
        loadBulkData();
        List<Data> allData = new ArrayList<Data>(store.readAll());
        Collections.sort(allData);
        Assert.assertEquals(6, allData.size());
        Assert.assertEquals("name", allData.get(0).getName());
        Assert.assertEquals("name2", allData.get(5).getName());

    }

    @Test
    public void testRemove() throws InterruptedException, JSONException {
        loadBulkData();
        store.remove(1);

        List<Data> allData = new ArrayList<Data>(store.readAll());
        Collections.sort(allData);
        Assert.assertEquals(5, allData.size());
        Assert.assertEquals(2l, (long) allData.get(0).getId());
        Assert.assertEquals("name2", allData.get(4).getName());

    }

    @Test
    public void testFilter() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<Data> result;

        loadBulkData();

        result = store.readWithFilter(null);
        Assert.assertEquals(6, result.size());

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("name", "name2");
        filter.setWhere(where);
        result = store.readWithFilter(filter);
        Assert.assertEquals(3, result.size());

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("name", "name2");
        where.put("description", "description");
        filter.setWhere(where);
        result = store.readWithFilter(filter);
        Assert.assertEquals(2, result.size());

    }

    @Test
    public void testNestedSaveAndFilter() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<TrivialNestedClass> result;

        Data data = new Data(10, "name", "description");

        TrivialNestedClass newNested = new TrivialNestedClass();
        newNested.setId(1);
        newNested.setText("nestedText");
        newNested.setData(data);

        open(nestedStore);
        nestedStore.save(newNested);

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("text", "nestedText");
        JSONObject dataFilter = new JSONObject();
        dataFilter.put("id", 10);
        where.put("data", dataFilter);
        filter.setWhere(where);
        result = nestedStore.readWithFilter(filter);
        Assert.assertEquals(1, result.size());
        TrivialNestedClass nestedResult = result.get(0);
        Assert.assertEquals("name", nestedResult.data.getName());

    }

    @Test
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

        open(nestedWithCollectionStore);
        nestedWithCollectionStore.save(newNested);

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("text", "nestedText");
        where.put("id", 1);

        filter.setWhere(where);
        result = nestedWithCollectionStore.readWithFilter(filter);
        Assert.assertEquals(1, result.size());
        TrivialNestedClassWithCollection nestedResult = result.get(0);
        Assert.assertEquals((Integer) 10, nestedResult.data.get(0).getId());
        Assert.assertEquals((Integer) 30, nestedResult.data.get(1).getId());

    }

    @Test
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

        latch.await(5, TimeUnit.SECONDS);
        Assert.assertEquals("OnSuccess should be called exactly once!", 1, latch.getCount());

    }

    private void saveData(Integer id, String name, String desc) throws InterruptedException {
        open(store);
        store.save(new Data(id, name, desc));
    }

    private void saveData(Integer id, String name, String desc, boolean enable) throws InterruptedException {
        open(store);
        store.save(new Data(id, name, desc, enable));
    }

    @Test
    public void testSaveListOfBoringData() throws InterruptedException {
        SQLStore<ListWithId> longStore = new SQLStore<ListWithId>(ListWithId.class, context);
        open(longStore);
        ListWithId<Long> longList = new ListWithId<Long>(100);

        longList.setId(1);

        for (long i = 0; i < 100; i++) {
            longList.data.add(i);
        }
        longStore.save(longList);
        Assert.assertEquals(100, longStore.readAll().iterator().next().data.size());

    }

    private <T> void open(SQLStore<T> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        store.open(new Callback<SQLStore<T>>() {
            @Override
            public void onSuccess(SQLStore<T> sqlStore) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                throw new RuntimeException(e);
            }
        });
        latch.await();
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
