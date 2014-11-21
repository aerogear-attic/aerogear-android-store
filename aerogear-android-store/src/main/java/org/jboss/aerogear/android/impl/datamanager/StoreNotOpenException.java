package org.jboss.aerogear.android.impl.datamanager;

public class StoreNotOpenException extends RuntimeException {

    public StoreNotOpenException() {
        super("The store is not opened");
    }

}
