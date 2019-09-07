package com.oop.orangeengine.main.player;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.events.AsyncEvents;
import com.oop.orangeengine.main.util.DefaultInitialization;
import com.oop.orangeengine.main.util.OptionalConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerController extends AEngineComponent {

    @DefaultInitialization
    public PlayerController() {
        super();

        AsyncEvents.listen(PlayerEvent.class, event -> {

           if(event instanceof PlayerMoveEvent) return;
           lookup(event.getPlayer()).ifPresent(player -> {

               Consumer<Event> consumer = player.getRegisteredConsumers().get(event.getClass());
               if(consumer != null)
                   consumer.accept(event);

           });

        });
    }

    private OptionalConsumer<OPlayer> lookup(Player player) {
        return lookup(player.getUniqueId());
    }

    private Set<OPlayer> playerSet = new HashSet<>();

    @Override
    public String getName() {
        return "PlayerController";
    }

    public OptionalConsumer<OPlayer> lookup(UUID uuid) {
        return OptionalConsumer.of(playerSet.stream()
                .filter(p -> p.getUuid() == uuid).
                        findFirst());
    }

    public OPlayer lookupInsert(UUID uuid) {

        OptionalConsumer<OPlayer> oPlayer = lookup(uuid);
        if(!oPlayer.isPresent()) {

            oPlayer.accept(Optional.of(OPlayer.of(uuid)));
            playerSet.add(oPlayer.get());

        }

        return oPlayer.get();

    }

}
