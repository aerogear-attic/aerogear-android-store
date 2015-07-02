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
package org.jboss.aerogear.android.store.test.memory;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Assert;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.test.helper.Data;
import org.jboss.aerogear.android.store.test.helper.DataWithNoIdConfigured;
import org.jboss.aerogear.android.store.test.helper.DataWithNoPropertyId;
import org.jboss.aerogear.android.core.reflection.PropertyNotFoundException;
import org.jboss.aerogear.android.core.reflection.RecordIdNotFoundException;
import org.jboss.aerogear.android.store.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.store.memory.MemoryStore;
import org.jboss.aerogear.android.store.test.MainActivity;
import org.jboss.aerogear.android.store.test.generator.StubIdGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MemoryStoreTest extends PatchedActivityInstrumentationTestCase {

    private Store<Data> store;
    private StubIdGenerator stubIdGenerator;

    public MemoryStoreTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        stubIdGenerator = new StubIdGenerator();
        store = new MemoryStore<Data>(stubIdGenerator);
    }

    @Test
    public void testReadAll() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Collection<Data> datas = store.readAll();
        Assert.assertNotNull("datas could not be null", datas);
        Assert.assertEquals("datas should 2 data", 2, datas.size());
    }

    @Test
    public void testReadWithFilter() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Collection<Data> datas = store.readWithFilter(new ReadFilter());
        Assert.assertNotNull("datas could not be null", datas);
        Assert.assertEquals("datas should 2 data", 2, datas.size());
    }

    @Test
    public void testReadWithFilterPerPage() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        ReadFilter filter = new ReadFilter();
        filter.setLimit(1);

        Collection<Data> datas = store.readWithFilter(filter);
        Assert.assertNotNull("datas could not be null", datas);
        Assert.assertEquals("datas should 1 data", 1, datas.size());
        Assert.assertEquals("foo", datas.iterator().next().getName());

        filter.setOffset(1);
        datas = store.readWithFilter(filter);
        Assert.assertEquals("bar", datas.iterator().next().getName());
    }

    @Test
    public void testReadWithFilterWhere() throws JSONException {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        ReadFilter filter = new ReadFilter();
        filter.setWhere(new JSONObject("{\"name\":\"bar\"}"));

        Collection<Data> datas = store.readWithFilter(filter);

        Assert.assertNotNull("datas could not be null", datas);
        Assert.assertEquals("datas should 1 data", 1, datas.size());
        Assert.assertEquals("bar", datas.iterator().next().getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadWithFilterThrowsExceptionWithNestingJSON() throws JSONException {

        ReadFilter filter = new ReadFilter();
        filter.setWhere(new JSONObject("{\"name\":{\"name\":\"bar\"}}"));
        store.readWithFilter(filter);

    }

    @Test
    public void testRead() {
        store.save(new Data("foo", "desc of foo"));
        Data data = store.read(1);
        Assert.assertNotNull("data could not be null", data);
    }

    @Test
    public void testSaveNoExistRecord() {
        Data data = new Data("foo", "desc of foo");
        store.save(data);
        Assert.assertEquals(Integer.valueOf(1), data.getId());
    }

    @Test
    public void testSaveExistRecord() {
        Data data = new Data("foo", "desc of foo");
        store.save(data);

        // Simulate sending the same id but not necessarily the same instance
        data = new Data(1, "bar", "desc of bar");
        store.save(data);

        data = store.read(1);

        Assert.assertEquals(Integer.valueOf(1), data.getId());
        Assert.assertEquals("bar", data.getName());
        Assert.assertEquals("desc of bar", data.getDescription());
    }

    @Test(expected = RecordIdNotFoundException.class)
    public void testSaveWithAnnotationNotConfigured() {

        MemoryStore<DataWithNoIdConfigured> memoryStore = new MemoryStore<DataWithNoIdConfigured>(stubIdGenerator);
        memoryStore.save(new DataWithNoIdConfigured());

    }

    @Test(expected = PropertyNotFoundException.class)
    public void testSaveWithNoPropertyToSetId() {
        MemoryStore<DataWithNoPropertyId> memoryStore = new MemoryStore<DataWithNoPropertyId>(stubIdGenerator);
        memoryStore.save(new DataWithNoPropertyId());

    }

    @Test
    public void testReset() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Data foo = store.read(1);
        Assert.assertNotNull("foo could not be null", foo);

        Data bar = store.read(2);
        Assert.assertNotNull("bar could not be null", bar);

        store.reset();

        foo = store.read(1);
        Assert.assertNull("foo should be null", foo);

        bar = store.read(2);
        Assert.assertNull("bar should be null", bar);
    }

    @Test
    public void testRemove() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Data foo = store.read(1);
        Assert.assertNotNull("foo could not be null", foo);

        Data bar = store.read(2);
        Assert.assertNotNull("bar could not be null", bar);

        store.remove(2);

        foo = store.read(1);
        Assert.assertNotNull("foo could not be null", foo);

        bar = store.read(2);
        Assert.assertNull("bar should be null", bar);
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue("should be empty", store.isEmpty());
    }

    @Test
    public void testIsNotEmpty() {
        store.save(new Data("foo", "desc of foo"));
        Assert.assertFalse("should not be empty", store.isEmpty());
    }

    @Test
    public void testSaveCollection() {
        List<Data> items = new ArrayList<Data>();
        items.add(new Data(1, "Item 1", "This is the item 1"));
        items.add(new Data(2, "Item 2", "This is the item 2"));
        items.add(new Data(3, "Item 3", "This is the item 3"));
        items.add(new Data(4, "Item 4", "This is the item 4"));
        items.add(new Data(5, "Item 5", "This is the item 5"));
        store.save(items);

        Assert.assertEquals("Should have " + items.size() + " items", items.size(), store.readAll().size());
    }

}
