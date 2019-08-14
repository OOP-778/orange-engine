package com.oop.orangeengine.item;

import com.oop.orangeengine.main.Helper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static org.bukkit.Bukkit.getServer;

public class ItemStackUtil {
    protected static final Class<?> CLASS_CRAFT_ITEM;
    private static final String
            ERR_NBT_LOAD = "Failed to find a load method in NBTCompressedStreamTools",
            ERR_NBT_SAVE = "Failed to find a save method in NBTCompressedStreamTools";
    private static final Constructor<?> CONSTRUCTOR_NBT;
    private static final Method
            METHOD_NBT_SAVE,
            METHOD_NBT_LOAD,
            METHOD_NBT_SET_STRING;
    private static final Class<?> CLASS_ITEM;
    private static final Method

            METHOD_ITEM_TO,
            METHOD_ITEM_FROM,
            METHOD_ITEM_SAVE;
    private static final Method LOAD_ITEM_FROM_JSON;
    private static Constructor<?> CONSTRUCTOR_ITEM;
    private static Method METHOD_ITEM_CREATE;

    static {
        try {
            final String version = getServer().getClass().getName().split("\\.")[3];
            final String nms = "net.minecraft.server.%s.%s";
            final String cb = "org.bukkit.craftbukkit.%s.%s";

            final Class<?> nbt = Class(nms, version, "NBTTagCompound");
            CONSTRUCTOR_NBT = nbt.getConstructor();
            CONSTRUCTOR_NBT.setAccessible(true);
            METHOD_NBT_SET_STRING = nbt.getDeclaredMethod("setString", String.class, String.class);
            Method nbtSave = null, nbtLoad = null;
            for (Method method : Class(nms, version, "NBTCompressedStreamTools").getDeclaredMethods()) {
                final Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && params[1] == DataOutput.class)
                    nbtSave = method;
                else if (params.length == 1 && params[0] == DataInputStream.class)
                    nbtLoad = method;
            }
            if ((METHOD_NBT_SAVE = nbtSave) == null)
                throw new IllegalStateException(ERR_NBT_SAVE);
            if ((METHOD_NBT_LOAD = nbtLoad) == null)
                throw new IllegalStateException(ERR_NBT_LOAD);
            METHOD_NBT_SAVE.setAccessible(true);
            METHOD_NBT_LOAD.setAccessible(true);

            CLASS_ITEM = Class(nms, version, "ItemStack");
            CLASS_CRAFT_ITEM = Class(cb, version, "inventory.CraftItemStack");
            METHOD_ITEM_FROM = CLASS_CRAFT_ITEM.getDeclaredMethod("asBukkitCopy", CLASS_ITEM);
            METHOD_ITEM_TO = CLASS_CRAFT_ITEM.getDeclaredMethod("asNMSCopy", ItemStack.class);

            Class<?> mojangsonParser = Class(nms, version, "MojangsonParser");
            LOAD_ITEM_FROM_JSON = mojangsonParser.getDeclaredMethod("parse", String.class);

            try {
                CONSTRUCTOR_ITEM = CLASS_ITEM.getConstructor(nbt);
                CONSTRUCTOR_ITEM.setAccessible(true);
                METHOD_ITEM_CREATE = null;
            } catch (Throwable ignored) {
                CONSTRUCTOR_ITEM = null;
                METHOD_ITEM_CREATE = CLASS_ITEM.getDeclaredMethod("createStack", nbt);
                METHOD_ITEM_CREATE.setAccessible(true);
            }
            METHOD_ITEM_SAVE = CLASS_ITEM.getDeclaredMethod("save", nbt);
            METHOD_ITEM_SAVE.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize reflection!", e);
        }
    }

    private static Class<?> Class(String format, String version, String name) throws ClassNotFoundException {
        return forName(format(format, version, name));
    }

    public static Object createNBTTagCompound() throws Exception {
        return CONSTRUCTOR_NBT.newInstance();
    }

    public static void saveNBT(OutputStream out, Object nbt) throws Exception {
        METHOD_NBT_SAVE.invoke(null, nbt, new DataOutputStream(out));
    }

    public static Object loadNBT(InputStream in) throws Exception {
        return METHOD_NBT_LOAD.invoke(null, new DataInputStream(in));
    }

    public static void setString(Object nbt, String key, String value) throws Exception {
        METHOD_NBT_SET_STRING.invoke(nbt, key, value);
    }

    public static Object itemFromBukkit(ItemStack item) throws Exception {
        return METHOD_ITEM_TO.invoke(null, item);
    }

    public static ItemStack itemToBukkit(Object item) throws Exception {
        return (ItemStack) METHOD_ITEM_FROM.invoke(null, item);
    }

    public static void saveItems(ItemStack[] contents, OutputStream out) throws Exception {
        final ByteBuffer length = ByteBuffer.allocate(4);
        length.putInt(contents.length);
        out.write(length.array());
        for (ItemStack item : contents)
            saveItem(item, out);
    }

    public static ItemStack[] loadItems(InputStream in) throws Exception {
        final ByteBuffer length = ByteBuffer.allocate(4);
        in.read(length.array());
        final ItemStack[] contents = new ItemStack[length.getInt()];
        for (int i = 0; i < contents.length; i++)
            contents[i] = loadItem(in);
        return contents;
    }


    public static void saveItem(ItemStack item, OutputStream out) throws Exception {

        if (item == null || item.getType() == Material.AIR) return;
        Helper.print(itemStackToJson(item));

        try {
            Helper.print("ITEM FROM BUKKIT " + itemFromBukkit(item));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        saveNBT(out, METHOD_ITEM_SAVE.invoke(itemFromBukkit(item), createNBTTagCompound()));
    }

    public static ItemStack loadItem(InputStream in) throws Exception {
        return itemToBukkit(CONSTRUCTOR_ITEM == null ?
                METHOD_ITEM_CREATE.invoke(null, loadNBT(in)) :
                CONSTRUCTOR_ITEM.newInstance(loadNBT(in))
        );
    }

    public static String itemStackToJson(ItemStack item) throws Exception {
        if (item == null || item.getType() == Material.AIR) return null;
        return METHOD_ITEM_SAVE.invoke(itemFromBukkit(item), createNBTTagCompound()).toString();
    }

    public static ItemStack itemStackFromJson(String input) {
        try {
            Object nbt = LOAD_ITEM_FROM_JSON.invoke(null, input);
            return itemToBukkit(CONSTRUCTOR_ITEM == null ?
                    METHOD_ITEM_CREATE.invoke(null, nbt) :
                    CONSTRUCTOR_ITEM.newInstance(nbt));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public static boolean isSimilar(ItemStack item, ItemStack item2) {

        if (item.getType() != item2.getType()) return false;
        if (item.getDurability() != item2.getDurability()) return false;

        if (item.hasItemMeta() && item2.hasItemMeta()) {

            if (item.getItemMeta().hasDisplayName() && item2.getItemMeta().hasDisplayName()) {
                if (!item.getItemMeta().getDisplayName().equalsIgnoreCase(item2.getItemMeta().getDisplayName()))
                    return false;
            }

            if (item.getItemMeta().hasLore() && item2.getItemMeta().hasLore()) {
                return !item.getItemMeta().getLore().equals(item2.getItemMeta().getLore());
            }

        }

        return true;

    }

}
