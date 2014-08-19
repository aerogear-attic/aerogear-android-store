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
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreFactory;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;

import java.net.MalformedURLException;

import static junit.framework.Assert.*;
import static org.jboss.aerogear.android.impl.datamanager.StoreTypes.MEMORY;
import static org.jboss.aerogear.android.impl.datamanager.StoreTypes.SQL;
import org.jboss.aerogear.android.store.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.store.MainActivity;

public class DataManagerTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public DataManagerTest() {
        super(MainActivity.class);
    }
    private DataManager dataManager;

    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        dataManager = new DataManager();
    }

    public void testConstructors() throws Exception {
        IdGenerator defaultGenerator = new DefaultIdGenerator();
        StoreFactory defaultFactory = new DefaultStoreFactory();
        DataManager manager = new DataManager(defaultGenerator);
        assertEquals(defaultGenerator, UnitTestUtils.getPrivateField(manager,
                            "idGenerator", IdGenerator.class));

        manager = new DataManager(defaultFactory);
        assertEquals(defaultFactory, UnitTestUtils.getPrivateField(manager,
                            "storeFactory", StoreFactory.class));

        manager = new DataManager(defaultGenerator, defaultFactory);
        assertEquals(defaultFactory, UnitTestUtils.getPrivateField(manager,
                            "storeFactory", StoreFactory.class));
        assertEquals(defaultGenerator, UnitTestUtils.getPrivateField(manager,
                            "idGenerator", IdGenerator.class));
    }

    public void testRegisterStoreFactory() throws MalformedURLException {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        DataManager dataManager = new DataManager(new StubStoreFactory());

        Store store = dataManager.store("stub store", Data.class);

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", "Stub", store.getType().getName());
    }

    public void testCreateStoreWithDefaultType() {
        Store store = dataManager.store("foo", Data.class);

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    public void testCreateStoreWithMemoryType() {
        Store store = dataManager.store("foo", new StoreConfig(Data.class));

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    public void testAddStoreWithDefaultType() {
        dataManager.store("foo", Data.class);
        Store store = dataManager.get("foo");

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    public void testAddStoreWithMemoryType() {
        dataManager.store("foo", new StoreConfig(Data.class));
        Store store = dataManager.get("foo");

        assertNotNull("foo store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    public void testAddStoreWithSQLType() {
        StoreConfig sqlStoreConfig = new StoreConfig(Data.class);
        sqlStoreConfig.setContext(super.getActivity().getApplicationContext());
        sqlStoreConfig.setType(SQL);
        dataManager.store("foo", sqlStoreConfig);
        Store store = dataManager.get("foo");
        assertNotNull("foo store could not be null", store);
        assertEquals("verifying the type", SQL, store.getType());
    }

    public void testAndAddAndRemoveStores() {
        dataManager.store("foo", new StoreConfig(Data.class));
        dataManager.store("bar", Data.class);

        Store fooStore = dataManager.get("foo");
        assertNotNull("foo store could not be null", fooStore);

        Store barStore = dataManager.get("bar");
        assertNotNull("bar store could not be null", barStore);

        fooStore = dataManager.remove("foo");
        assertNotNull("foo store could not be null", fooStore);

        fooStore = dataManager.get("foo");
        assertNull("foo store should be null", fooStore);
    }

}
