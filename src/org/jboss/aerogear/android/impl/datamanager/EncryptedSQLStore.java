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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.GsonBuilder;
import org.jboss.aerogear.AeroGearCrypto;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreType;
import org.jboss.aerogear.android.impl.crypto.InvalidKeyException;
import org.jboss.aerogear.android.impl.reflection.Property;
import org.jboss.aerogear.android.impl.reflection.Scan;
import org.jboss.aerogear.android.impl.util.CryptoUtils;
import org.jboss.aerogear.crypto.Random;
import org.jboss.aerogear.crypto.keys.PrivateKey;
import org.jboss.aerogear.crypto.password.Pbkdf2;

import java.io.Serializable;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EncryptedSQLStore<T> extends SQLiteOpenHelper implements Store<T> {

    private final Class<T> modelClass;
    private final GsonBuilder builder;
    private final IdGenerator idGenerator;
    private final String passphrase;

    private CryptoUtils<T> cryptoUtils;

    private final String ENCRYPT_HELPER_TABLE_SUFIX = "_ENCRYPT_HELPER";

    private final String TABLE_NAME;
    private final String COLUMN_ID = "ID";
    private final String COLUMN_DATA = "DATA";

    private final String ID_IV = "IV";
    private final String ID_SALT = "SALT";

    public EncryptedSQLStore(Class<T> modelClass, Context context, GsonBuilder builder,
                             IdGenerator idGenerator, String passphrase) {

        super(context, modelClass.getSimpleName(), null, 1);

        this.modelClass = modelClass;
        this.builder = builder;
        this.idGenerator = idGenerator;
        this.passphrase = passphrase;

        this.TABLE_NAME = modelClass.getSimpleName();
    }
    
    public EncryptedSQLStore(Class<T> modelClass, Context context, GsonBuilder builder,
                             IdGenerator idGenerator, String passphrase, String tableName) {

        super(context, modelClass.getSimpleName(), null, 1);

        this.modelClass = modelClass;
        this.builder = builder;
        this.idGenerator = idGenerator;
        this.passphrase = passphrase;

        this.TABLE_NAME = tableName;
    }

    private String getEncryptTableHelperName() {
        return TABLE_NAME.toUpperCase() + ENCRYPT_HELPER_TABLE_SUFIX;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        byte[] salt = new Random().randomBytes();
        byte[] iv = new Random().randomBytes();

        String SQL_CREATE_ENCRYPT_HELPER_TABLE = "CREATE TABLE IF NOT EXISTS " + getEncryptTableHelperName() +
                " ( " +
                COLUMN_ID + " TEXT NOT NULL, " +
                COLUMN_DATA + " BLOB NOT NULL " +
                " ) ";
        sqLiteDatabase.execSQL(SQL_CREATE_ENCRYPT_HELPER_TABLE);

        String SQL_STORE_IV = "INSERT INTO " + getEncryptTableHelperName() +
                " ( " + COLUMN_ID + ", " + COLUMN_DATA + " ) " +
                " VALUES ( ?, ? ) ";
        sqLiteDatabase.execSQL(SQL_STORE_IV, new Object[] { ID_IV, iv });

        String SQL_STORE_SALT = "INSERT INTO " + getEncryptTableHelperName() +
                " ( " + COLUMN_ID + ", " + COLUMN_DATA + " ) " +
                " VALUES ( ?, ? ) ";
        sqLiteDatabase.execSQL(SQL_STORE_SALT, new Object[] { ID_SALT, salt });

        String SQL_CREATE_ENTITY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " ( " +
                COLUMN_ID + " TEXT NOT NULL, " +
                COLUMN_DATA + " BLOB NOT NULL " +
                " ) ";
        sqLiteDatabase.execSQL(SQL_CREATE_ENTITY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        byte[] iv;
        byte[] salt;

        String SQL = "SELECT " + COLUMN_DATA + " FROM " + getEncryptTableHelperName() + " WHERE " + COLUMN_ID + " = ?";

        Cursor cursorIV = db.rawQuery(SQL, new String[] { ID_IV });
        cursorIV.moveToFirst();

        try {
            iv = cursorIV.getBlob(0);
        } finally {
            cursorIV.close();
        }

        Cursor cursorSalt = db.rawQuery(SQL, new String[] { ID_SALT });
        cursorSalt.moveToFirst();

        try {
            salt = cursorSalt.getBlob(0);
        } finally {
            cursorSalt.close();
        }

        try {
            Pbkdf2 pbkdf2 = AeroGearCrypto.pbkdf2();
            byte[] rawPassword = pbkdf2.encrypt(passphrase, salt);
            PrivateKey privateKey = new PrivateKey(rawPassword);
            cryptoUtils = new CryptoUtils<T>(privateKey, iv, modelClass, builder);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreType getType() {
        return StoreTypes.ENCRYPTED_SQL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> readAll() throws InvalidKeyException {
        ArrayList<T> dataList = new ArrayList<T>();

        String sql = "SELECT " + COLUMN_DATA + " FROM " + TABLE_NAME;
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[0]);
        try {
            while (cursor.moveToNext()) {
                byte[] encryptedData = cursor.getBlob(0);
                T decryptedData = cryptoUtils.decrypt(encryptedData);
                dataList.add(decryptedData);
            }
        } finally {
            cursor.close();
        }

        return dataList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T read(Serializable id) throws InvalidKeyException {
        String sql = "SELECT " + COLUMN_DATA + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[] { id.toString() });
        cursor.moveToFirst();

        if (cursor.getCount() == 0) {
            return null;
        }

        try {
            byte[] encryptedData = cursor.getBlob(0);
            return cryptoUtils.decrypt(encryptedData);
        } finally {
            cursor.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> readWithFilter(ReadFilter filter) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(T item) {
        String recordIdFieldName = Scan.recordIdFieldNameIn(item.getClass());
        Property property = new Property(item.getClass(), recordIdFieldName);
        Serializable idValue = (Serializable) property.getValue(item);

        if (idValue == null) {
            idValue = idGenerator.generate();
            property.setValue(item, idValue);
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, idValue.toString());
        values.put(COLUMN_DATA, cryptoUtils.encrypt(item));

        getWritableDatabase().insert(TABLE_NAME, null, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        String sql = String.format("DELETE FROM " + TABLE_NAME);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Serializable id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        getWritableDatabase().execSQL(sql, new Object[] { id });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        String sql = "SELECT COUNT(" + COLUMN_ID + ") FROM " + TABLE_NAME;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToFirst();
        boolean result = (cursor.getInt(0) == 0);
        cursor.close();
        return result;
    }

}
