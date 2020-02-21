package com.oop.orangeengine.database.newversion.handler;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public interface DataHandler<O extends Object> {

    /**
     * Serializes object to string (preferably json)
     * @param o the instance of O type object
     * @return serialized string
     */
    String serialize(@Nullable Field field, O o) throws Throwable;

    /**
     * Deserializes object from string to O type
     * @param json a serialized object O
     * @return deserialized object
     */
    O deserialize(@Nullable Field field, String json) throws Throwable;

    /**
     * Returns class of O type
     * @return returns class of O type
     */
    Class<O> getClazz();

}
