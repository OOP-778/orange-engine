package com.oop.orangeengine.database.gson;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

public class FactoryUtil {
    public static JsonPrimitive wrapDefault(Object object) {
        if (object instanceof String) return new JsonPrimitive(object.toString());
        if (object instanceof Integer) return new JsonPrimitive(object.toString() + "i");
        if (object instanceof Double) return new JsonPrimitive(object.toString() + "d");
        if (object instanceof Float) return new JsonPrimitive(object.toString() + "f");
        if (object instanceof Long) return new JsonPrimitive(object.toString() + "l");
        if (object instanceof Boolean) return new JsonPrimitive(((Boolean) object ? "1" : "0") + "b");
        else return null;
    }

    public static Object unwrapDefault(String json) {
        if (json.contentEquals("null")) return null;

        char endsWith = json.charAt(json.length() - 1);
        String cut = json.substring(0, json.length() - 1);

        try {
            if (endsWith == 'i')
                return Integer.valueOf(cut);

            else if (endsWith == 'd')
                return Double.valueOf(cut);

            else if (endsWith == 'f')
                return Float.valueOf(cut);

            else if (endsWith == 'l')
                return Long.valueOf(cut);

            else if (endsWith == 'b')
                return cut.toCharArray()[0] == '1' ? Boolean.TRUE : Boolean.FALSE;

            else return json;

        } catch (Throwable throwable) {
            return json;
        }
    }

    public static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(jsonString);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    public static String toPrettyFormat(JsonElement element) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(element);
    }

    public static JsonElement readElement(JsonReader in) throws IOException {
        in.peek();
        switch (in.peek()) {
            case BEGIN_OBJECT:
                JsonObject jsonObject = new JsonObject();

                in.beginObject();
                while (in.hasNext())
                    jsonObject.add(in.nextName(), readElement(in));
                in.endObject();

                return jsonObject;

            case NULL:
                in.nextNull();
                return JsonNull.INSTANCE;

            case NUMBER:
                return new JsonPrimitive(in.nextString() + "un");

            case BOOLEAN:
                return new JsonPrimitive(in.nextBoolean() ? "1" : "0");

            case STRING:
                return new JsonPrimitive(in.nextString());

            case BEGIN_ARRAY:
                JsonArray array = new JsonArray();

                in.beginArray();
                while (in.hasNext())
                    array.add(readElement(in));
                in.endArray();

                return array;

            case END_DOCUMENT:
            case NAME:
            case END_OBJECT:
            case END_ARRAY:

            default:
                throw new IllegalStateException(in.peek() + " at " + in.toString());
        }
    }
}
