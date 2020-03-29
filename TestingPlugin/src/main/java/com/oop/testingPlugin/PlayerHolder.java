package com.oop.testingPlugin;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.DatabaseController;
import com.oop.orangeengine.database.DatabaseHolder;
import com.oop.orangeengine.database.DatabaseObject;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PlayerHolder implements DatabaseHolder<UUID, StatsPlayer> {

    private ConcurrentHashMap<UUID, StatsPlayer> dataMap = new ConcurrentHashMap<>();
    private final DatabaseController databaseController;

    @Override
    public Stream<StatsPlayer> dataStream() {
        return dataMap.values().stream();
    }

    @Override
    public UUID generatePrimaryKey(StatsPlayer object) {
        return object.getUuid();
    }

    @Override
    public Set<Class<? extends DatabaseObject>> getObjectVariants() {
        return Sets.newHashSet(StatsPlayer.class);
    }

    @Override
    public DatabaseController getDatabaseController() {
        return databaseController;
    }

    @Override
    public void onAdd(StatsPlayer object, boolean isNew) {
        dataMap.put(object.getUuid(), object);
    }

    @Override
    public void onRemove(StatsPlayer object) {
        dataMap.remove(object.getUuid());
    }

    public StatsPlayer getOrInsert(Player player) {
        StatsPlayer statsPlayer = dataMap.get(player.getUniqueId());
        if (statsPlayer == null) {
            statsPlayer = new StatsPlayer(player.getUniqueId());
            add(statsPlayer, true);
        }

        return statsPlayer;
    }
}
