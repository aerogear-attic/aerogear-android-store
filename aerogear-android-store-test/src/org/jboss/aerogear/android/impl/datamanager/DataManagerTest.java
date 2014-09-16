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


import org.jboss.aerogear.android.DataManager;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.store.MainActivity;
import org.jboss.aerogear.android.store.impl.util.PatchedActivityInstrumentationTestCase;

import static org.jboss.aerogear.android.impl.datamanager.StoreTypes.MEMORY;
import static org.jboss.aerogear.android.impl.datamanager.StoreTypes.SQL;

public class DataManagerTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public DataManagerTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testCreateStoreWithDefaultType() {
        Store store = DataManager
                .config("foo1", MemoryStoreConfig.class)
                .createMemoryStore();

        assertNotNull("store could not be null", store);
    }

    public void testCreateStoreWithMemoryType() {
        Store store = DataManager
                .config("foo2", MemoryStoreConfig.class)
                .createMemoryStore();

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    public void testAddStoreWithMemoryType() {
        DataManager
                .config("foo3", MemoryStoreConfig.class)
                .createMemoryStore();

        Store store = DataManager.getStore("foo3");

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    public void testAddStoreWithSQLType() {
        DataManager
                .config("foo4", SQLStoreConfig.class)
                .setKlass(Data.class)
                .setContext(super.getActivity().getApplicationContext())
                .createSQLStore();

        Store store = DataManager.getStore("foo4");

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", SQL, store.getType());
    }

}
