package com.oop.orangeengine.item.custom;

import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.util.version.OVersion;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.nbt.NBTCompound;
import com.oop.orangeengine.nbt.NBTCompoundList;
import com.oop.orangeengine.nbt.NBTItem;
import com.oop.orangeengine.nbt.NBTListCompound;
import com.oop.orangeengine.yaml.ConfigSection;
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

    public OSkull(OSkull from) {
        super(from.getItemStack().clone());
        this.texture = from.texture;
    }

    public OSkull(@NonNull ItemStack item) {
        super(item);
    }

    public OSkull() {
        super(OMaterial.PLAYER_HEAD);
    }

    public OSkull(@NonNull Player player) {
        super(OMaterial.PLAYER_HEAD);
        getOMaterial().parseMaterial();
        texture(player);
    }

    public OSkull(String texture) {
        super(OMaterial.PLAYER_HEAD);
        texture(texture);
    }

    public OSkull texture(Player player) {
        SkullMeta itemMeta = getItemMeta();
        itemMeta.setOwner(player.getName());
        return this;
    }

    public OSkull texture(String texture) {
        this.texture = texture == null ? defaultTexture : texture;
        texture = this.texture;

        NBTItem nbi = getNbt();
        NBTCompound skull = nbi.addCompound("SkullOwner");
        UUID id = new UUID(texture().hashCode(), texture().hashCode());

        if (OVersion.isOrAfter(16))
            skull.setIntArray("Id", _16_fromUUID(id));
        else
            skull.setString("Id", id.toString());

        NBTCompoundList compoundList = skull.addCompound("Properties").getCompoundList("textures");
        compoundList.clear();

        NBTListCompound textures = compoundList.addCompound();
        textures.setString("Value", texture);
        return this;
    }

    @Override
    public OSkull load(ConfigSection section) {
        super.load(section);

        // Load texture
        section.ifValuePresent("texture", String.class, this::texture);
        return this;
    }

    @Override
    public SkullMeta getItemMeta() {
        return (SkullMeta) super.getItemMeta();
    }

    private int[] _16_fromUUID(UUID uuid) {
        long MSB = uuid.getMostSignificantBits(), LSB = uuid.getLeastSignificantBits();
        return new int[]{(int)(MSB >> 32), (int)MSB, (int)(LSB >> 32), (int)LSB};
    }

    @Override
    public void onClone(OSkull from) {
        if (from.texture() != null)
            this.texture = from.texture;
    }
}
