package com.oop.orangeengine.main.gson;

import com.google.gson.*;
import com.oop.orangeengine.nbt.NBTContainer;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.NonNull;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    private static final String UNICODE_REGEX = "\\\\u([0-9a-f]{4})";
    private static final Pattern UNICODE_PATTERN = Pattern.compile(UNICODE_REGEX);

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return NBTItem.convertNBTtoItem(new NBTContainer(utf8(jsonElement.getAsString())));
    }

    @Override
    public JsonElement serialize(@NonNull ItemStack itemStack, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(NBTItem.convertItemtoNBT(itemStack).asNBTString());
    }

    public static String utf8(String text) {
        Matcher matcher = UNICODE_PATTERN.matcher(text);
        StringBuffer decodedMessage = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(
                    decodedMessage, String.valueOf((char) Integer.parseInt(matcher.group(1), 16)));
        }
        matcher.appendTail(decodedMessage);
        return decodedMessage.toString();
    }

}
