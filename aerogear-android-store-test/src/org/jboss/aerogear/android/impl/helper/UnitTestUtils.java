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
package org.jboss.aerogear.android.impl.helper;

import org.jboss.aerogear.android.impl.reflection.FieldNotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnitTestUtils {

    public static void setPrivateField(Object target, String fieldName,
            Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        List<Field> fields = getAllFields(new ArrayList<Field>(), target.getClass());

        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                field.set(target, value);
                return;
            }
        }

        throw new FieldNotFoundException(target.getClass(), fieldName);

    }

    public static Object getPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static Object getSuperPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static <T> T getPrivateField(Object target, String fieldName,
            Class<T> type) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        Collections.addAll(fields, type.getDeclaredFields());

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    public static Object callPrivateMethod(Object instance, String methodName, Class[] types, Object[] params) {
        try {

            Method method = instance.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(instance, params);
        } catch (Exception ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.FINEST, null, ex);
            throw new RuntimeException(ex);
        }

    }

    static boolean equals(Object object1, Object object2) {
        
        if (object1 == object2) {//Same object or both null
            return true;
        } else if (object1 == null || object2 == null) {//One or the other is null but not both
            return false;
        } 
        
        return object1.equals(object2);
        
    }

}