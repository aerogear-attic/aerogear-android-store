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
package org.jboss.aerogear.android.store;

import org.jboss.aerogear.android.core.ConfigurationProvider;
import org.jboss.aerogear.android.store.memory.EncryptedMemoryStoreConfiguration;
import org.jboss.aerogear.android.store.memory.EncryptedMemoryStoreConfigurationProvider;
import org.jboss.aerogear.android.store.memory.MemoryStoreConfiguration;
import org.jboss.aerogear.android.store.memory.MemoryStoreConfigurationProvider;
import org.jboss.aerogear.android.store.sql.EncryptedSQLStoreConfiguration;
import org.jboss.aerogear.android.store.sql.EncryptedSQLStoreConfigurationProvider;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;
import org.jboss.aerogear.android.store.sql.SQLStoreConfigurationProvider;

import java.util.HashMap;
import java.util.Map;

public final class DataManager {

    private static Map<String, Store<?>> stores = new HashMap<String, Store<?>>();

    private static Map<Class<? extends StoreConfiguration<?>>, ConfigurationProvider<?>> configurationProviderMap = new HashMap<Class<? extends StoreConfiguration<?>>, ConfigurationProvider<?>>();

    private static OnStoreCreatedListener onStoreCreatedListener = new OnStoreCreatedListener() {
        @Override
        public void onStoreCreated(StoreConfiguration<?> configuration, Store<?> store) {
            stores.put(configuration.getName(), store);
        }
    };

    static {
        DataManager.registerConfigurationProvider(MemoryStoreConfiguration.class,
                new MemoryStoreConfigurationProvider());
        DataManager.registerConfigurationProvider(SQLStoreConfiguration.class,
                new SQLStoreConfigurationProvider());
        DataManager.registerConfigurationProvider(EncryptedMemoryStoreConfiguration.class,
                new EncryptedMemoryStoreConfigurationProvider());
        DataManager.registerConfigurationProvider(EncryptedSQLStoreConfiguration.class,
                new EncryptedSQLStoreConfigurationProvider());
    }

    private DataManager() {
    }

    public static <CONFIGURATION extends StoreConfiguration<CONFIGURATION>> void registerConfigurationProvider
            (Class<CONFIGURATION> configurationClass, ConfigurationProvider<CONFIGURATION> provider) {
        configurationProviderMap.put(configurationClass, provider);
    }

    public static <CFG extends StoreConfiguration<CFG>> CFG config(String name, Class<CFG> storeImplementationClass) {

        @SuppressWarnings("unchecked")
        ConfigurationProvider<? extends StoreConfiguration<CFG>> provider =
                (ConfigurationProvider<? extends StoreConfiguration<CFG>>)
                configurationProviderMap.get(storeImplementationClass);

        if (provider == null) {
            throw new IllegalArgumentException("Configuration not registered!");
        }

        return provider.newConfiguration()
                .setName(name)
                .addOnStoreCreatedListener(onStoreCreatedListener);

    }

    public static Store getStore(String name) {
        return stores.get(name);
    }

}
