package com.oop.orangeengine.item.custom;

import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.nbt.NBTCompound;
import com.oop.orangeengine.nbt.NBTCompoundList;
import com.oop.orangeengine.nbt.NBTItem;
import com.oop.orangeengine.nbt.NBTListCompound;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class OSkull extends ItemBuilder<OSkull> {

    private final String defaultTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19";
    private String texture;

    public OSkull(@NonNull ItemStack item) {
        super(item);
    }

    public OSkull() {
        super(OMaterial.PLAYER_HEAD);
    }

    public OSkull(@NonNull Player player) {
        super(OMaterial.PLAYER_HEAD);

        texture(player);
    }

    public OSkull(String texture) {
        super(OMaterial.PLAYER_HEAD);
        texture(texture);
    }

    public OSkull texture(Player player) {
        SkullMeta itemMeta = getItemMeta();
        itemMeta.setOwner(player.getName());
        itemMeta(itemMeta);
        return _returnThis();
    }

    public OSkull texture(String texture) {
        this.texture = texture == null ? defaultTexture : texture;

        NBTItem skull = new NBTItem(getItemStack());
        NBTCompound skullOwner = skull.addCompound("SkullOwner");
        skullOwner.setString("Id", new UUID(texture().hashCode(), texture().hashCode()).toString());

        NBTCompoundList textures = skull.addCompound("Properties").getCompoundList("textures");
        NBTListCompound signature = textures.addCompound();
        signature.setString("Value", texture);

        setItemStack(skull.getItem());
        return _returnThis();
    }

    @Override
    public OSkull load(ConfigurationSection section) {
       super.load(section);

       // Load texture
        section.ifValuePresent("texture", String.class, this::texture);

        return _returnThis();
    }

    @Override
    protected OSkull _returnThis() {
        return this;
    }

    @Override
    public SkullMeta getItemMeta() {
        return (SkullMeta) super.getItemMeta();
    }
}
