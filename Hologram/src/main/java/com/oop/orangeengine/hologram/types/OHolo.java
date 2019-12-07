package com.oop.orangeengine.hologram.types;

import com.oop.orangeengine.hologram.HoloLine;
import com.oop.orangeengine.hologram.Viewable;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.Updateable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class OHolo implements Viewable, Updateable {

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
        if(lineList.isEmpty()) return new ArrayList<>();
        return lineList.get(0).getViewers();
    }

    public OptionalConsumer<HoloLine> getLineThatMatches(Predicate<HoloLine> filter) {
        return OptionalConsumer.of(
                lineList.stream()
                    .filter(filter)
                    .findFirst()
        );
    }

    @Override
    public void update() {
        lineList.forEach(HoloLine::update);
    }
}
