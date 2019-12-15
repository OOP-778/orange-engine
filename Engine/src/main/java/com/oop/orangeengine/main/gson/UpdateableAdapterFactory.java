package com.oop.orangeengine.main.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class UpdateableAdapterFactory implements TypeAdapterFactory {

    private final Gson gson = new GsonBuilder().serializeNulls().registerTypeHierarchyAdapter(ItemStack.class, new BukkitAdapter.ItemStackAdapter()).create();

    public UpdateableAdapterFactory(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapterFactory(this);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (GsonUpdateable.class.isAssignableFrom(typeToken.getRawType()))
            return (TypeAdapter<T>) adapter;

        return null;
    }

    private final TypeAdapter<GsonUpdateable> adapter = new TypeAdapter<GsonUpdateable>() {
        @Override
        public void write(JsonWriter jsonWriter, GsonUpdateable gsonUpdateable) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("clazz");
            jsonWriter.value(gsonUpdateable.getClass().getName());
            jsonWriter.name("data");
            jsonWriter.value(gson.toJson(gsonUpdateable).replace("{", ">").replace("}", "<"));
            jsonWriter.endObject();
        }

        @Override
        public GsonUpdateable read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            jsonReader.nextName();
            final String clazzPath = jsonReader.nextString();

            jsonReader.nextName();
            final String serializedObject = jsonReader.nextString();
            jsonReader.endObject();

            Class<?> clazz = null;
            try {
                clazz = Class.forName(clazzPath);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (clazz == null) return null;

            GsonUpdateable gsonUpdateable = (GsonUpdateable) gson.fromJson(serializedObject.replace(">", "{").replace("<","}"), clazz);
            gsonUpdateable.loadFields();
            return gsonUpdateable;
        }
    };
}

