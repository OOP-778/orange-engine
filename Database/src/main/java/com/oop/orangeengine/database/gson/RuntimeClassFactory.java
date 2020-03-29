package com.oop.orangeengine.database.gson;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.oop.orangeengine.database.suppliers.Suppliable;
import com.oop.orangeengine.main.util.OSimpleReflection;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class RuntimeClassFactory<T> implements TypeAdapterFactory {
    private final Class<?> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

    private RuntimeClassFactory(Class<?> baseType, String typeFieldName) {
        if (typeFieldName == null || baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
    }

    /**
     * Creates a new runtime type adapter using for {@code baseType} using {@code
     * typeFieldName} as the type field name. Type field names are case sensitive.
     */
    public static <T> RuntimeClassFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new RuntimeClassFactory<T>(baseType, typeFieldName);
    }

    /**
     * Creates a new runtime type adapter for {@code baseType} using {@code "type"} as
     * the type field name.
     */
    public static <T> RuntimeClassFactory<T> of(Class<T> baseType) {
        return new RuntimeClassFactory<T>(baseType, "class");
    }

    /**
     * Registers {@code type} identified by {@code label}. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or {@code label}
     *                                  have already been registered on this type adapter.
     */
    public RuntimeClassFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type == null || label == null) {
            throw new NullPointerException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        return this;
    }

    /**
     * Registers {@code type} identified by its {@link Class#getSimpleName simple
     * name}. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or its simple name
     *                                  have already been registered on this type adapter.
     */
    public RuntimeClassFactory<T> registerSubtype(Class<? extends T> type) {
        return registerSubtype(type, type.getSimpleName());
    }

    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        final Map<String, TypeAdapter<?>> labelToDelegate
                = new LinkedHashMap<String, TypeAdapter<?>>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate
                = new LinkedHashMap<Class<?>, TypeAdapter<?>>();

        if (Object.class.isAssignableFrom(type.getRawType())) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, type);
            labelToDelegate.put(type.getRawType().getName(), delegate);
            subtypeToDelegate.put(type.getRawType(), delegate);
        }

        return new TypeAdapter<R>() {
            @SuppressWarnings("unchecked")
            @Override
            public R read(JsonReader in) throws IOException {
                JsonElement jsonElement = Streams.parse(in);

                if (jsonElement.isJsonObject()) {
                    JsonElement labelJsonElement = jsonElement.getAsJsonObject().remove(typeFieldName);
                    if (labelJsonElement == null) {
                        throw new JsonParseException("cannot deserialize " + baseType
                                + " because it does not define a field named " + typeFieldName);
                    }
                    String label = labelJsonElement.getAsString();
                    Class<R> aClass;
                    try {
                        aClass = (Class<R>) Class.forName(label);
                    } catch (ClassNotFoundException e) {
                        throw new JsonParseException("Cannot find class " + label, e);
                    }

                    if (aClass.isEnum()) {
                        return (R) findProperEnum((Class<? extends Enum>) aClass, jsonElement.getAsJsonObject().getAsJsonPrimitive("value").getAsString());
                    }

                    TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                    if (delegate == null) {
                        TypeToken<R> subClass = TypeToken.get(aClass);
                        delegate = gson.getDelegateAdapter(RuntimeClassFactory.this, subClass);
                        if (delegate == null) {
                            throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
                                    + label + "; did you forget to register a subtype?");
                        }
                    }
                    System.out.println(delegate);
                    R object = delegate.fromJsonTree(jsonElement);
                    if (object instanceof Suppliable) {
                        System.out.println("Loading supplier!");
                        ((Suppliable) object)._loadSupplier();
                    }

                    return object;

                } else if (jsonElement.isJsonNull()) {
                    return null;

                } else {
                    TypeAdapter<R> delegate = gson.getDelegateAdapter(RuntimeClassFactory.this, type);
                    if (delegate == null) {
                        throw new JsonParseException("cannot deserialize " + baseType + "; did you forget to register a subtype?");
                    }
                    R object = delegate.fromJsonTree(jsonElement);
                    if (object instanceof Suppliable) {
                        System.out.println("Loading supplier!");
                        ((Suppliable) object)._loadSupplier();
                    }

                    return object;
                }
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                String label = srcType.getName();

                TypeAdapter<R> delegate = getDelegate(srcType);
                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did you forget to register a subtype?");
                }

                JsonElement jsonTree = delegate.toJsonTree(value);
                System.out.println(srcType);
                if (!jsonTree.isJsonObject()) {
                    if (srcType.isEnum()) {
                        System.out.println("Working with enum!");
                        JsonObject jsonObject = new JsonObject();

                        jsonObject.addProperty("value", ((Enum)value).name());
                        jsonObject.add(typeFieldName, new JsonPrimitive(label));

                        System.out.println("Writing enum: " + ((Enum)value).name());
                        Streams.write(jsonObject, out);

                    } else
                        Streams.write(jsonTree, out);

                } else {
                    JsonObject jsonObject = jsonTree.getAsJsonObject();
                    JsonObject clone = new JsonObject();
                    for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                        clone.add(e.getKey(), e.getValue());
                    }
                    if (!clone.has(typeFieldName))
                        clone.add(typeFieldName, new JsonPrimitive(label));
                    Streams.write(clone, out);
                }
            }

            @SuppressWarnings("unchecked")
            private TypeAdapter<R> getDelegate(Class<?> srcType) {
                TypeAdapter<?> typeAdapter = subtypeToDelegate.get(srcType);
                if (typeAdapter != null)
                    return (TypeAdapter<R>) typeAdapter;

                for (Map.Entry<Class<?>, TypeAdapter<?>> classTypeAdapterEntry : subtypeToDelegate.entrySet()) {
                    if (classTypeAdapterEntry.getKey().isAssignableFrom(srcType)) {
                        return (TypeAdapter<R>) classTypeAdapterEntry.getValue();
                    }
                }
                return null;
            }
        }.nullSafe();
    }

    private Enum findProperEnum(Class<? extends Enum> aClass, String value) {
        // First try to find by normal constant
        try {
            return  Enum.valueOf(aClass, value);
        } catch (IllegalArgumentException ignored) {}

        // Secondly try to find with uppercase
        try {
            return Enum.valueOf(aClass, value.toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        // Thirdly (The most unefficient)
        try {
            Method valuesMethod = OSimpleReflection.getMethod(aClass, "values");
            Object[] values = (Object[]) valuesMethod.invoke(null);

            for (Object o : values) {
                if (String.valueOf(o).equalsIgnoreCase(value))
                    return (Enum) o;
            }

        } catch (Throwable thrw) {
            throw new JsonParseException("Failed to parse enum by class " + aClass, thrw);
        }

        return null;
    }
}
