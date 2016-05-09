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

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.memory.EncryptedMemoryStore;
import org.jboss.aerogear.android.store.memory.EncryptedMemoryStoreConfiguration;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;
import org.jboss.aerogear.android.store.test.MainActivity;
import org.jboss.aerogear.android.store.test.generator.StubIdGenerator;
import org.jboss.aerogear.android.store.test.helper.Data;
import org.jboss.aerogear.android.store.test.util.PatchedActivityInstrumentationTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class EncryptedMemoryStoreTest extends PatchedActivityInstrumentationTestCase {

    private Store<Data> store;

    public EncryptedMemoryStoreTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        Context context = new RenamingDelegatingContext(getActivity(), UUID.randomUUID().toString());

        StubIdGenerator stubIdGenerator = new StubIdGenerator();
        String passphrase = "Lorem Ipsum";
        Class<Data> dataModel = Data.class;

        store = new EncryptedMemoryStore<Data>(context, stubIdGenerator, passphrase, dataModel);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutKlass() {

        Store<Data> store1 = DataManager.config("store1", EncryptedMemoryStoreConfiguration.class)
                .usingPassword("AeroGear")
                .store(null);

        Data data = new Data(10, "name", "description");
        store1.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutPassphrase() {

        Store<Data> store2 = DataManager.config("store2", EncryptedMemoryStoreConfiguration.class)
                .store(Data.class);

        Data data = new Data(10, "name", "description");
        store2.save(data);

    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSQLStoreWithoutPassphraseAndKlass() {

        Store<Data> store3 = DataManager.config("store3", SQLStoreConfiguration.class).store(null);

        Data data = new Data(10, "name", "description");
        store3.save(data);

    }

    @Test
    public void testReadAll() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Collection<Data> datas = store.readAll();
        Assert.assertNotNull("datas could not be null", datas);
        Assert.assertEquals("datas should 2 data", 2, datas.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReadWithFilter() {
        store.readWithFilter(new ReadFilter());

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
