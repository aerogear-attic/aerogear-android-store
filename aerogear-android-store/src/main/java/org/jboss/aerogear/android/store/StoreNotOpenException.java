package org.jboss.aerogear.android.store;

public class StoreNotOpenException extends RuntimeException {

    public StoreNotOpenException() {
        super("The store is not opened");
    }

}
