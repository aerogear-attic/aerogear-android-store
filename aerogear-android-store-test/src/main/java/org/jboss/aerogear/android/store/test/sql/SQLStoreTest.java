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
import android.os.StrictMode;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.core.RecordId;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.generator.DefaultIdGenerator;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.jboss.aerogear.android.store.test.helper.Data;
import org.jboss.aerogear.android.store.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;
import org.jboss.aerogear.android.store.test.MainActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SQLStoreTest extends PatchedActivityInstrumentationTestCase {

    public SQLStoreTest() {
        super(MainActivity.class);
    }

    private Context context;
    private SQLStore<Data> store;
    private SQLStore<TrivialNestedClass> nestedStore;
    private SQLStore<TrivialNestedClassWithCollection> nestedWithCollectionStore;

    private static final StrictMode.VmPolicy DEFAULT_VM_POLICY = StrictMode.getVmPolicy();
    
    
    private static final StrictMode.VmPolicy STRICT_VM_POLICY = new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build();
    

    @Before
    public void setUp() throws Exception {

        StrictMode.setVmPolicy(STRICT_VM_POLICY);
        
        Assume.assumeTrue(!System.getProperty("os.name").toLowerCase().startsWith("mac os x") || !System.getProperty("java.version").startsWith("1.7.0"));
        this.context = new RenamingDelegatingContext(getActivity(), UUID.randomUUID().toString());

        this.store = (SQLStore) DataManager
                .config("store", SQLStoreConfiguration.class)
                .withContext(context)
                .store(Data.class);

        this.nestedStore = (SQLStore) DataManager
                .config("trivialNestedClass", SQLStoreConfiguration.class)
                .withContext(context)
                .store(TrivialNestedClass.class);

        this.nestedWithCollectionStore = (SQLStore) DataManager
                .config("trivialNestedClassWithCollection", SQLStoreConfiguration.class)
                .withContext(context)
                .store(TrivialNestedClassWithCollection.class);
    }

    @After
    public void closeAll() {
        this.store.close();
        this.nestedStore.close();
        this.nestedWithCollectionStore.close();

        StrictMode.setVmPolicy(DEFAULT_VM_POLICY);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutKlass() {

        Store<Data> store1 = DataManager.config("store1", SQLStoreConfiguration.class)
                .withContext(context)
                .store(null);

        Data data = new Data(10, "name", "description");
        store1.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutContext() {

        Store<Data> store2 = DataManager.config("store2", SQLStoreConfiguration.class)
                .store(Data.class);

        Data data = new Data(10, "name", "description");
        store2.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutContextAndKlass() {

        Store<Data> store3 = DataManager.config("store3", SQLStoreConfiguration.class).store(null);

        Data data = new Data(10, "name", "description");
        store3.save(data);

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
        store.openSync();
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

        nestedWithCollectionStore.openSync();
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
        Assert.assertEquals(1, result.size());
        TrivialNestedClassWithCollection nestedResult = result.get(0);
        Assert.assertEquals((Integer) 10, nestedResult.data.get(0).getId());
        Assert.assertEquals((Integer) 30, nestedResult.data.get(1).getId());

    }

    @Test
    public void testSuccessCallback() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        ((SQLStore) store).open(new Callback<SQLStore<Data>>() {
            @Override
            public void onSuccess(SQLStore<Data> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException(e);
            }
        });

        Assert.assertFalse("OnSuccess should be called exactly once!", latch.await(5, TimeUnit.SECONDS));

    }

    private void saveData(Integer id, String name, String desc) throws InterruptedException {
        store.save(new Data(id, name, desc));
    }

    private void saveData(Integer id, String name, String desc, boolean enable) throws InterruptedException {
        store.openSync();
        store.save(new Data(id, name, desc, enable));
    }

    @Test
    public void testSaveListOfBoringData() throws InterruptedException {
        SQLStore<ListWithId> longStore = new SQLStore<ListWithId>(ListWithId.class, context);
        longStore.openSync();
        ListWithId<Long> longList = new ListWithId<Long>(100);

        longList.setId(1);

        for (long i = 0; i < 100; i++) {
            longList.data.add(i);
        }
        longStore.save(longList);
        Assert.assertEquals(100, longStore.readAll().iterator().next().data.size());
        longStore.close();

    }

    @Test
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
        store.close();
    }

    @Test
    public void testSaveItemWithMap() {
        TrivialNestedMap trivialNestedMap = new TrivialNestedMap();
        trivialNestedMap.setId(1);
        Map<String, String> data = new HashMap<>();
        trivialNestedMap.setData(data);

        String key = "dot.breaks";
        data.put(key, "value");
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(type, MapAsArrayTypeAdapter.get(String.class, String.class));
        SQLStore<TrivialNestedMap> store = new SQLStore<>(TrivialNestedMap.class, context, gsonBuilder, new DefaultIdGenerator());
        store.openSync();
        store.save(trivialNestedMap);

        Collection<TrivialNestedMap> result = store.readAll();

        Assert.assertTrue(!result.isEmpty());
        Assert.assertTrue(result.iterator().next().getData().containsKey(key));
        store.close();
    }

    private void loadBulkData() throws InterruptedException {
        saveData(1, "name", "description");
        saveData(2, "name", "description");
        saveData(3, "name2", "description");
        saveData(4, "name2", "description");
        saveData(5, "name", "description2");
        saveData(6, "name2", "description2");
    }

    public static final class TrivialNestedMap {
        @RecordId
        private Integer id;
        private Map<String, String> data;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }
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

    public static final class MapAsArrayTypeAdapter<K, V>
            implements JsonSerializer<Map<K, V>>, JsonDeserializer<Map<K, V>> {
        private final TypeToken<K> keyType;
        private final TypeToken<V> valueType;

        private MapAsArrayTypeAdapter(TypeToken<K> keyType, TypeToken<V> valueType) {
            this.keyType = keyType;
            this.valueType = valueType;
        }

        public static <K, V> MapAsArrayTypeAdapter<K, V> get(Class<K> keyType, Class<V> valueType) {
            return new MapAsArrayTypeAdapter<K, V>(TypeToken.get(keyType), TypeToken.get(valueType));
        }

        public static <K, V> MapAsArrayTypeAdapter<K, V> get(
                TypeToken<K> keyType, TypeToken<V> valueType) {
            return new MapAsArrayTypeAdapter<K, V>(keyType, valueType);
        }

        public Map<K, V> deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            Map<K, V> result = new LinkedHashMap<K, V>();
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i+=2) {
                result.put(context.<K>deserialize(array.get(i), keyType.getType()),
                        context.<V>deserialize(array.get(i + 1), valueType.getType()));
            }
            return result;
        }

        public JsonElement serialize(Map<K, V> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray result = new JsonArray();
            for (Map.Entry<K, V> entry : src.entrySet()) {
                result.add(context.serialize(entry.getKey(), keyType.getType()));
                result.add(context.serialize(entry.getValue(), valueType.getType()));
            }
            return result;
        }
    }

}
