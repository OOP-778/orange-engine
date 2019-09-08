package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.util.DefaultInitialization;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
import com.oop.orangeengine.menu.button.impl.FillableButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.function.Consumer;

public class InventoryController extends AEngineComponent {

    private Map<String, List<OPair<String, Consumer<ButtonClickEvent>>>> clickHandler = new HashMap<>();

    @DefaultInitialization
    public InventoryController() {

        OLogger logger = getEngine().getLogger();
        logger.printWarning("Registering Inventory Controller...");

        SyncEvents.listen(InventoryClickEvent.class, EventPriority.LOWEST, event -> {

            if (event.getSlot() < 0) return;
            if (!(event.getClickedInventory().getHolder() instanceof AMenu)) return;

            AMenu menu = (AMenu) event.getClickedInventory().getHolder();
            WrappedInventory wrappedInventory = menu.getWrapperFromBukkit(event.getClickedInventory());

            AMenuButton button = wrappedInventory.getButtonAt(event.getSlot());
            if (button.currentItem().getType() == Material.AIR && !(button instanceof FillableButton)) return;

            if (!button.pickable())
                event.setCancelled(true);

            ButtonClickEvent buttonClickEvent = new ButtonClickEvent(wrappedInventory, menu, event, (Player) event.getWhoClicked());
            Bukkit.getPluginManager().callEvent(buttonClickEvent);

            if (buttonClickEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            // Global click event
            menu.globalClickHandler().accept(buttonClickEvent);

            // Per button click event
            button.clickHandler().getAsOptional(ClickEnum.GLOBAL).ifPresent(handler -> handler.accept(buttonClickEvent));

            ClickEnum clickEnum = ClickEnum.match(event);
            if (clickEnum != ClickEnum.GLOBAL)
                button.clickHandler().getAsOptional(clickEnum).ifPresent(handler -> handler.accept(buttonClickEvent));

            if (button.sound() != null)
                button.sound().play((Location) event.getWhoClicked());
        });

        getEngine().getOwning().onDisable(() -> Helper.getOnlinePlayers().stream()
                .filter(player -> player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof AMenu)
                .forEach(HumanEntity::closeInventory));
    }

    @Override
    public String getName() {
        return "Inventory Controller";
    }

    public void addClickHandler(String menuName, String actionIdentity, Consumer<ButtonClickEvent> consumer) {
        List<OPair<String, Consumer<ButtonClickEvent>>> menuHandler = clickHandler.computeIfAbsent(menuName, k -> new ArrayList<>());
        menuHandler.add(new OPair<>(actionIdentity, consumer));
    }

    public void addClickHandlerGlobal(String actionIdentity, Consumer<ButtonClickEvent> consumer) {
        List<OPair<String, Consumer<ButtonClickEvent>>> menuHandler = clickHandler.computeIfAbsent("global", k -> new ArrayList<>());
        menuHandler.add(new OPair<>(actionIdentity, consumer));
    }

    public OptionalConsumer<Consumer<ButtonClickEvent>> findClickHandler(String actionName, String menuName) {
        List<OPair<String, Consumer<ButtonClickEvent>>> handler = clickHandler.get(menuName);
        if (handler == null)
            handler = clickHandler.get("global");

        return OptionalConsumer.of(
                handler.stream()
                        .filter(pair -> pair.getFirst().equalsIgnoreCase(actionName))
                        .map(OPair::getSecond)
                        .findFirst()
        );

    }

}
