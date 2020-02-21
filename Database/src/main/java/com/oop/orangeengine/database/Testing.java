package com.oop.orangeengine.database;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Testing {

    public static void main(String[] args) {
        try {
            TestObject testObject = new TestObject();
            Gson gson = new GsonBuilder().registerTypeAdapterFactory(RuntimeClassNameTypeAdapterFactory.of(Object.class)).create();
            String serialized = gson.toJson(testObject);
            Object object = gson.fromJson(serialized, Object.class);
        } catch (Throwable thrw) {
            thrw.printStackTrace();
        }
    }

    public static class ObjHandler implements JsonSerializer<Object> {

        private Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeHierarchyAdapter(Object.class, this)
                .create();

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement jsonElement = gson.toJsonTree(src);
            JsonObject o = (JsonObject) jsonElement;
            o.addProperty("clazz", typeOfSrc.getTypeName());

            return o;
        }
    }

    public static class TestObject {

        private TestOgb2 ob = new TestOgb2();

    }

    public static class TestOgb2 {
        private int awhgaw = 252525;
        private boolean oof = false;

        private TestOgb3 ob = new TestOgb3();
    }

    public static class TestOgb3 {
        private int awhgaw = 252525;
        private boolean oof = false;

        private List<String> ooof = Arrays.asList("awgawgawg", "Wgawgawg", "Awgawgawgag");
    }

    public static final class RuntimeClassNameTypeAdapterFactory<T> implements TypeAdapterFactory {
        private final Class<?> baseType;
        private final String typeFieldName;
        private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
        private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

        private RuntimeClassNameTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
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
        public static <T> RuntimeClassNameTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
            return new RuntimeClassNameTypeAdapterFactory<T>(baseType, typeFieldName);
        }

        /**
         * Creates a new runtime type adapter for {@code baseType} using {@code "type"} as
         * the type field name.
         */
        public static <T> RuntimeClassNameTypeAdapterFactory<T> of(Class<T> baseType) {
            return new RuntimeClassNameTypeAdapterFactory<T>(baseType, "class");
        }

        /**
         * Registers {@code type} identified by {@code label}. Labels are case
         * sensitive.
         *
         * @throws IllegalArgumentException if either {@code type} or {@code label}
         *                                  have already been registered on this type adapter.
         */
        public RuntimeClassNameTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
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
        public RuntimeClassNameTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
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
                        TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                        if (delegate == null) {
                            Class<R> aClass;
                            try {
                                aClass = (Class<R>) Class.forName(label);
                            } catch (ClassNotFoundException e) {
                                throw new JsonParseException("Cannot find class " + label, e);
                            }

                            TypeToken<R> subClass = TypeToken.get(aClass);
                            delegate = gson.getDelegateAdapter(RuntimeClassNameTypeAdapterFactory.this, subClass);
                            if (delegate == null) {
                                throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
                                        + label + "; did you forget to register a subtype?");
                            }
                        }
                        return delegate.fromJsonTree(jsonElement);
                    } else if (jsonElement.isJsonNull()) {
                        return null;
                    } else {
                        TypeAdapter<R> delegate = gson.getDelegateAdapter(RuntimeClassNameTypeAdapterFactory.this, type);
                        if (delegate == null) {
                            throw new JsonParseException("cannot deserialize " + baseType + "; did you forget to register a subtype?");
                        }
                        System.out.println(baseType);
                        return delegate.fromJsonTree(jsonElement);
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
                    if (!jsonTree.isJsonObject()) {
                        Streams.write(jsonTree, out);

                    } else {
                        JsonObject jsonObject = jsonTree.getAsJsonObject();
                        if (jsonObject.has(typeFieldName)) {
                            throw new JsonParseException("cannot serialize " + srcType.getName()
                                    + " because it already defines a field named " + typeFieldName);
                        }
                        JsonObject clone = new JsonObject();
                        clone.add(typeFieldName, new JsonPrimitive(label));
                        for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                            clone.add(e.getKey(), e.getValue());
                        }
                        Streams.write(clone, out);
                    }
                }

                @SuppressWarnings("unchecked")
                private TypeAdapter<R> getDelegate(Class<?> srcType) {
                    TypeAdapter<?> typeAdapter = subtypeToDelegate.get(srcType);
                    if (typeAdapter != null) {
                        return (TypeAdapter<R>) typeAdapter;
                    }

                    for (Map.Entry<Class<?>, TypeAdapter<?>> classTypeAdapterEntry : subtypeToDelegate.entrySet()) {
                        if (classTypeAdapterEntry.getKey().isAssignableFrom(srcType)) {
                            return (TypeAdapter<R>) classTypeAdapterEntry.getValue();
                        }
                    }
                    return null;
                }
            }.nullSafe();
        }
    }

}
