package com.orangeengine.hologram;

import java.util.List;
import java.util.UUID;

public interface Viewable {

    void addViewer(UUID uuid);

    void removeViewer(UUID uuid);

    List<UUID> getViewers();

}
