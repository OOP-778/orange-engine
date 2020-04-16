package com.oop.orangeengine.database.gson;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.oop.orangeengine.database.annotation.UniqueLabel;
import net.minecraft.server.v1_8_R3.BlockHopper;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftHopper;
import org.bukkit.material.MaterialData;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oop.orangeengine.database.gson.FactoryUtil.readElement;

public class ObjectFactory implements TypeAdapterFactory {

    public static final ObjectFactory FACTORY;
    static {
        FACTORY = new ObjectFactory();
    }

    private ObjectFactory() {}

    private static final String uniqueIdentifier = "uqlb";
    private static final String classIdentifier = "class";

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        rawType = Primitives.wrap(rawType);
        if (!Object.class.isAssignableFrom(rawType) && !rawType.isEnum()) return null;

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                // Null check
                if (value == null) {
                    Streams.write(new JsonPrimitive("null"), out);
                    return;
                }

                Class clazz = value.getClass();

                // Check for json primitives
                JsonPrimitive jsonPrimitive = FactoryUtil.wrapDefault(value);
                if (jsonPrimitive != null) {
                    Streams.write(jsonPrimitive, out);
                    return;
                }

                // We got more complex object
                // Check if we have to serialize it's class
                if (!ClassRegistry.isRegistered(clazz)) {
                    JsonElement jsonElement = ObjectFactory.this.toJsonTree(gson, value);
                    if (jsonElement.isJsonObject()) {
                        if (!jsonElement.getAsJsonObject().has(classIdentifier))
                            jsonElement.getAsJsonObject().addProperty(classIdentifier, clazz.getName());

                        Streams.write(jsonElement, out);

                    } else {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("class", clazz.getName());
                        jsonObject.add("value", jsonElement);

                        Streams.write(jsonObject, out);
                    }

                } else {
                    if (clazz.isEnum()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("enum", ((Enum)value).name());
                        Streams.write(jsonObject, out);

                    } else {
                        UniqueLabel uniqueLabel = value.getClass().getAnnotation(UniqueLabel.class);
                        JsonElement element = ObjectFactory.this.toJsonTree(gson, value);
                        if (uniqueLabel != null)
                            element.getAsJsonObject().addProperty(uniqueIdentifier, uniqueLabel.label());

                        Streams.write(element, out);
                    }
                }
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement jsonElement = readElement(in);

                // Check for default values
                if (jsonElement.isJsonPrimitive()) {
                    JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();

                    if (jsonPrimitive.isString())
                        return (T) FactoryUtil.unwrapDefault(jsonPrimitive.getAsString());

                } else if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    if (jsonObject.has("enum")) {
                        Enum anEnum = ClassRegistry.byValue(jsonObject.get("enum").getAsString());
                        return (T) anEnum;
                    }

                    // Stage 1) Look for a class
                    Class clazz = ObjectFactory.this.findClass(jsonObject);

                    // Stage 2) Look for unique label
                    if (clazz == null)
                        clazz = ObjectFactory.this.findClassByUniqueLabel(jsonObject);

                    // Stage 3) Look for a class by structure
                    if (clazz == null)
                        clazz = ClassRegistry.byStruct(jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

                    Preconditions.checkArgument(clazz != null, "Failed to find clazz for JsonObject: " + FactoryUtil.toPrettyFormat(jsonObject));
                    TypeAdapter delegateAdapter = gson.getDelegateAdapter(ObjectFactory.this, TypeToken.get(clazz));

                    JsonElement value = jsonObject.get("value");
                    if (value == null)
                        return (T) delegateAdapter.fromJsonTree(jsonObject);

                    else
                        return (T) delegateAdapter.fromJsonTree(value);
                }
                return null;
            }
        };
    }

    private Class findClass(JsonObject jsonObject) {
        if (jsonObject.has(classIdentifier)) {
            try {
                return Class.forName(jsonObject.get("class").getAsString());
            } catch (ClassNotFoundException ex) {
                return null;
            }
        }
        return null;
    }

    private Class findClassByUniqueLabel(JsonObject jsonObject) {
        if (jsonObject.has(uniqueIdentifier))
            return ClassRegistry.byUniqueIdentifier(jsonObject.get(uniqueIdentifier).getAsString());

        return null;
    }

    private JsonElement toJsonTree(Gson gson, Object object) {
        TypeAdapter delegateAdapter = gson.getDelegateAdapter(ObjectFactory.this, TypeToken.get(object.getClass()));
        return delegateAdapter.toJsonTree(object);
    }

    public Enum getEnum(Class<Enum> anEnum, String value) {
        try {
            return Enum.valueOf(anEnum, value);
        } catch (Throwable th) {
            return null;
        }
    }
}
