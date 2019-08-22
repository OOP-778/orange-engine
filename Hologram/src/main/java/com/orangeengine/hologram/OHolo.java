package com.orangeengine.hologram;

import javax.swing.text.View;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class OHolo implements Viewable {

    private final double spacing = 0.21D;
    private List<HoloLine> lineList = new LinkedList<>();

    @Override
    public void addViewer(UUID uuid) {
        lineList.forEach(line -> line.addViewer(uuid));
    }

    @Override
    public void removeViewer(UUID uuid) {
        lineList.forEach(line -> line.removeViewer(uuid));
    }

    @Override
    public List<UUID> getViewers() {
        return lineList.get(0).getViewers();
    }
}
