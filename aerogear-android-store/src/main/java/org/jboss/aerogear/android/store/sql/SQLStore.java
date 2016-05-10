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
package org.jboss.aerogear.android.store.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import com.google.gson.*;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.store.*;
import org.jboss.aerogear.android.core.reflection.Property;
import org.jboss.aerogear.android.core.reflection.Scan;
import org.jboss.aerogear.android.store.generator.DefaultIdGenerator;
import org.jboss.aerogear.android.store.generator.IdGenerator;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLStore<T> extends SQLiteOpenHelper implements Store<T> {

    private static final String TAG = SQLStore.class.getSimpleName();
    private final Class<T> klass;
    private final String className;
    private final static String CREATE_PROPERTIES_TABLE = "create table if not exists %s_property "
            + " ( _ID integer primary key autoincrement,"
            + "  PARENT_ID text not null,"
            + "  PROPERTY_NAME text not null,"
            + "  PROPERTY_VALUE text )";
    private final static String CREATE_PROPERTIES_INDEXES = "create index  if not exists %s_property_name_index "
            + " ON %s_property (PROPERTY_NAME);"
            + "create index if not exists %s_property_name_value_index "
            + " ON %s_property (PROPERTY_NAME, PROPERTY_VALUE) ;"
            + "create index  if not exists %s_property_parent_index "
            + " ON %s_property (PARENT_ID);";
    private SQLiteDatabase database;
    private final Gson gson;
    private final IdGenerator generator;

    public SQLStore(Class<T> klass, Context context) {
        super(context, klass.getSimpleName(), null, 1);
        this.klass = klass;
        this.className = klass.getSimpleName();
        this.gson = new Gson();
        this.generator = new DefaultIdGenerator();
    }

    public SQLStore(Class<T> klass, Context context, GsonBuilder builder, IdGenerator generator) {
        super(context, klass.getSimpleName(), null, 1);
        this.klass = klass;
        this.className = klass.getSimpleName();
        this.gson = builder.create();
        this.generator = generator;
    }

    public SQLStore(Class<T> klass, Context context, GsonBuilder builder, IdGenerator generator, String databaseName) {
        super(context, databaseName, null, 1);
        this.klass = klass;
        this.className = databaseName;
        this.gson = builder.create();
        this.generator = generator;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Collection<T> readAll(){
        ensureOpen();

        String sql = String.format("Select PROPERTY_NAME, PROPERTY_VALUE,PARENT_ID from %s_property", className);
        Cursor cursor = database.rawQuery(sql, new String[0]);
        HashMap<String, JsonObject> objects = new HashMap<String, JsonObject>(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(2);
                JsonObject object = objects.get(id);
                if (object == null) {
                    object = new JsonObject();
                    objects.put(id, object);
                }
                add(object, cursor.getString(0), cursor.getString(1));
            }
        } finally {
            cursor.close();
        }
        ArrayList<T> data = new ArrayList<T>(cursor.getCount());
        for (JsonObject object : objects.values()) {
            data.add(gson.fromJson(object, klass));
        }

        return data;

    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public T read(Serializable id) {
        ensureOpen();

        String sql = String.format("Select PROPERTY_NAME, PROPERTY_VALUE from %s_property where PARENT_ID = ?", className);
        String[] bindArgs = new String[1];
        bindArgs[0] = id.toString();
        JsonObject result = new JsonObject();
        Cursor cursor = database.rawQuery(sql, bindArgs);

        if (cursor.getCount() == 0) {
            return null;
        }

        try {
            while (cursor.moveToNext()) {
                add(result, cursor.getString(0), cursor.getString(1));
            }
        } finally {
            cursor.close();
        }

        return gson.fromJson(result, klass);

    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public List<T> readWithFilter(ReadFilter filter){
        ensureOpen();

        if (filter == null) {
            filter = new ReadFilter();
        }
        String sql = String.format("select PARENT_ID from %s_property where PROPERTY_NAME LIKE ? and PROPERTY_VALUE = ?", className);
        JsonObject where = (JsonObject) new JsonParser().parse(filter.getWhere().toString());
        List<Pair<String, String>> queryList = new ArrayList<Pair<String, String>>();
        Map<String, AtomicInteger> resultCount = new HashMap<String, AtomicInteger>();
        buildKeyValuePairs(where, queryList, "");

        if (queryList.isEmpty()) {// there is no query
            return new ArrayList<T>(readAll());
        } else {
            for (Pair<String, String> kv : queryList) {
                String[] bindArgs = new String[]{kv.first, kv.second};
                Cursor cursor = database.rawQuery(sql, bindArgs);
                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    AtomicInteger count = resultCount.get(id);
                    if (count == null) {
                        count = new AtomicInteger(0);
                        resultCount.put(id, count);
                    }
                    count.incrementAndGet();
                }
                cursor.close();
            }
        }
        List<T> results = new ArrayList<T>();

        for (String id : resultCount.keySet()) {
            if (resultCount.get(id).get() == queryList.size()) {// There are as many objects as queries which meant a result was returned for every query
                results.add(read(id));
            }
        }

        return results;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void save(T item){
        ensureOpen();

        this.database.beginTransaction();
        try {
            saveItem(item);
            this.database.setTransactionSuccessful();
        } finally {
            this.database.endTransaction();
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void save(Collection<T> items){
        ensureOpen();

        this.database.beginTransaction();
        try {
            for (T item : items) {
                saveItem(item);
            }
            this.database.setTransactionSuccessful();
        } finally {
            this.database.endTransaction();
        }
    }

    private void saveItem(T item) {
        String recordIdFieldName = Scan.recordIdFieldNameIn(item.getClass());
        Property property = new Property(item.getClass(), recordIdFieldName);
        Serializable idValue = (Serializable) property.getValue(item);

        if (idValue == null) {
            idValue = generator.generate();
            property.setValue(item, idValue);
        }

        JsonObject serialized = (JsonObject) gson.toJsonTree(item, klass);
        database.beginTransaction();
        try {
            saveElement(serialized, "", idValue);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private void saveElement(JsonElement serialized, String path, Serializable id) {
        String sql = String.format("insert into %s_property (PROPERTY_NAME, PROPERTY_VALUE, PARENT_ID) values (?,?,?)", className);

        if (serialized.isJsonObject()) {
            Set<Entry<String, JsonElement>> members = ((JsonObject) serialized).entrySet();
            String pathVar = path.isEmpty() ? "" : ".";

            for (Entry<String, JsonElement> member : members) {
                JsonElement jsonValue = member.getValue();
                String propertyName = member.getKey();

                if (jsonValue.isJsonArray()) {
                    JsonArray jsonArray = jsonValue.getAsJsonArray();
                    for (int index = 0; index < jsonArray.size(); index++) {
                        saveElement(jsonArray.get(index), path + pathVar + propertyName + String.format("[%d]", index), id);
                    }
                } else {
                    saveElement(jsonValue, path + pathVar + propertyName, id);
                }
            }
        } else if (serialized.isJsonPrimitive()) {
            JsonPrimitive primitive = serialized.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                String value = primitive.getAsBoolean() ? "true" : "false";
                database.execSQL(sql, new Object[]{path, value, id});
            } else if (primitive.isNumber()) {
                Number value = primitive.getAsNumber();
                database.execSQL(sql, new Object[]{path, value, id});
            } else if (primitive.isString()) {
                String value = primitive.getAsString();
                database.execSQL(sql, new Object[]{path, value, id});
            } else {
                throw new IllegalArgumentException(serialized + " isn't a number, boolean, or string");
            }
        } else {
            throw new IllegalArgumentException(serialized + " isn't a JsonObject or JsonPrimitive");
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void reset(){
        ensureOpen();

        String sql = String.format("Delete from %s_property", className);
        database.execSQL(sql);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public boolean isEmpty(){
        ensureOpen();

        String sql = String.format("Select count(_ID) from %s_property", className);
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        boolean result = (cursor.getInt(0) == 0);
        cursor.close();
        return result;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void remove(Serializable id){
        ensureOpen();

        String sql = String.format("Delete from %s_property where PARENT_ID = ?", className);
        Object[] bindArgs = new Object[1];
        bindArgs[0] = id;
        database.execSQL(sql, bindArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(CREATE_PROPERTIES_TABLE, className));
        db.execSQL(String.format(CREATE_PROPERTIES_INDEXES, className, className, className, className, className, className));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void open(final Callback<SQLStore<T>> onReady) {
        new AsyncTask<Void, Void, Void>() {
            private Exception exception;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    SQLStore.this.database = getWritableDatabase();
                } catch (Exception e) {
                    this.exception = e;
                    Log.e(TAG, "There was an error loading the database", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (exception != null) {
                    onReady.onFailure(exception);
                } else {
                    onReady.onSuccess(SQLStore.this);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void openSync() {
        this.database = getWritableDatabase();
    }

    @Override
    public void close() {
        this.database.close();
    }

    private void add(JsonObject result, String propertyName, String propertyValue) {
        if (!propertyName.contains(".")) {
            if (propertyName.contains("[")) {
                String unArrayPropertyName = propertyName.split("\\[")[0];
                if (!result.has(unArrayPropertyName)) {
                    result.add(unArrayPropertyName, new JsonArray());
                }
                JsonArray array = result.getAsJsonArray(unArrayPropertyName);
                array.add(gson.toJsonTree(propertyValue));
            } else {
                result.addProperty(propertyName, propertyValue);
            }
        } else {
            String[] names = propertyName.split("\\.", 2);

            if (names[0].contains("[")) {
                String key = names[0].split("\\[")[0];
                Integer index = Integer.parseInt(names[0].split("\\[")[1].split("\\]")[0]);
                JsonArray subObject = result.getAsJsonArray(key);

                if (subObject == null) {
                    subObject = new JsonArray();
                    result.add(key, subObject);
                }

                if ((index) >= subObject.size()) {
                    for (int i = subObject.size(); i < (index + 1); i++) {
                        subObject.add(new JsonObject());
                    }
                }

                JsonObject arrayItem = subObject.get(index).getAsJsonObject();

                add(arrayItem, names[1], propertyValue);

            } else {

                JsonObject subObject = (JsonObject) result.get(names[0]);
                if (subObject == null) {
                    subObject = new JsonObject();
                    result.add(names[0], subObject);
                }

                add(subObject, names[1], propertyValue);
            }
        }
    }

    private void buildKeyValuePairs(JsonObject where, List<Pair<String, String>> keyValues, String parentPath) {
        Set<Entry<String, JsonElement>> keys = where.entrySet();
        String pathVar = parentPath.isEmpty() ? "" : ".";// Set a dot if parent path is not empty
        for (Entry<String, JsonElement> entry : keys) {
            String key = entry.getKey();
            String path = parentPath + pathVar + key;
            JsonElement jsonValue = entry.getValue();
            if (jsonValue.isJsonObject()) {
                buildKeyValuePairs((JsonObject) jsonValue, keyValues, path);
            } else {
                if (jsonValue.isJsonPrimitive()) {
                    JsonPrimitive primitive = jsonValue.getAsJsonPrimitive();
                    if (primitive.isBoolean()) {
                        String value = primitive.getAsBoolean() ? "true" : "false";
                        keyValues.add(new Pair<String, String>(path, value));
                    } else if (primitive.isNumber()) {
                        Number value = primitive.getAsNumber();
                        keyValues.add(new Pair<String, String>(path, value.toString()));
                    } else if (primitive.isString()) {
                        String value = primitive.getAsString();
                        keyValues.add(new Pair<String, String>(path, value));
                    } else {
                        throw new IllegalArgumentException(jsonValue + " isn't a number, boolean, or string");
                    }

                } else {
                    throw new IllegalArgumentException(jsonValue + " isn't a JsonPrimitive");
                }

            }
        }
    }

    private boolean isOpen() {
        return this.database != null;
    }

    private void ensureOpen() {
        if (!isOpen()) {
            Log.w(TAG, "Store is not opened, trying to open.");
            openSync();
        }
    }

}
