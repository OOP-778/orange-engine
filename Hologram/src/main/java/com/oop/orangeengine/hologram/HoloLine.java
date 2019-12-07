package com.oop.orangeengine.hologram;

import com.oop.orangeengine.main.util.Updateable;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class HoloLine implements Viewable, Updateable {

    private String content;
    private List<UUID> viewers = new ArrayList<>();
    private final WrappedArmorStand wrappedArmorStand;

    public HoloLine(WrappedArmorStand wrappedArmorStand) {
        this.wrappedArmorStand = wrappedArmorStand;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void addViewer(UUID uuid) {
        removeViewer(uuid);
        viewers.add(uuid);
        wrappedArmorStand.spawn(Bukkit.getPlayer(uuid));
    }

    @Override
    public void removeViewer(UUID uuid) {
        viewers.remove(uuid);
    }

    @Override
    public void update() {
        wrappedArmorStand.update();
    }
}
