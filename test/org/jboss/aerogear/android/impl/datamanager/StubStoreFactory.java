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

import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreFactory;
import org.jboss.aerogear.android.datamanager.StoreType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StubStoreFactory implements StoreFactory {
    @Override
    public Store createStore(StoreConfig config) {
        return new Store() {
            @Override
            public StoreType getType() {
                return new StoreType() {
                    @Override
                    public String getName() {
                        return "Stub";
                    }
                };
            }

            @Override
            public Collection readAll() {
                return new ArrayList();
            }

            @Override
            public Object read(Serializable id) {
                return new Object();
            }

            @Override
            public void save(Object item) {
            }

            @Override
            public void reset() {
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public void remove(Serializable id) {
            }

            @Override
            public List readWithFilter(ReadFilter filter) {
                return new ArrayList();
            }

        };
    }
}
