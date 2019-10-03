package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.util.DefaultInitialization;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
import com.oop.orangeengine.menu.button.impl.FillableButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.MenuCloseEvent;
import com.oop.orangeengine.menu.events.MenuOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InventoryController extends AEngineComponent {

    private Map<String, List<OPair<String, Consumer<ButtonClickEvent>>>> clickHandler = new HashMap<>();

    @DefaultInitialization
    public InventoryController() {
        SyncEvents.listen(InventoryClickEvent.class, EventPriority.LOWEST, event -> {

            if (event.isCancelled()) return;
            if (event.getSlot() < 0) return;
            if (event.getWhoClicked().getOpenInventory().getTopInventory() == null) return;
            if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof AMenu)) return;

            if (event.getWhoClicked().getOpenInventory().getTopInventory() != null && event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof AMenu) {
                if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {

                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);

                    // Find possible items to stack inside player inventory
                    ItemStack cursor = event.getCursor().clone();
                    ItemStack originalCursor = event.getCursor().clone();

                    if (cursor.getType() != Material.AIR) {
                        if (cursor.getAmount() < 64) {

                            cursor.setAmount(cursor.getMaxStackSize() - cursor.getAmount());
                            int removed = removeFromInventory(cursor, event.getWhoClicked(), event.getSlot());

                            originalCursor.setAmount(originalCursor.getAmount() + removed);
                            event.getWhoClicked().setItemOnCursor(originalCursor);

                        }
                    }
                } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
                    event.setCancelled(true);
            }

            if (!(event.getClickedInventory().getHolder() instanceof AMenu)) return;

            event.setCancelled(true);
            AMenu menu = (AMenu) event.getClickedInventory().getHolder();
            WrappedInventory wrappedInventory = menu.getWrapperFromBukkit(event.getClickedInventory());

            AMenuButton button = wrappedInventory.getButtonAt(event.getSlot());
            if (button.currentItem().getType() == Material.AIR && !(button instanceof FillableButton)) return;
            ItemStack beforeChange = button.currentItem().clone();

            if (button.pickable()) {

                System.out.println("action: " + event.getAction());
                ItemStack currentAtSlot = event.getCurrentItem().clone();
                ItemStack cursor = event.getCursor().clone();
                int slot = event.getSlot();

                if (event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    event.getClickedInventory().setItem(slot, cursor);
                    event.getWhoClicked().setItemOnCursor(currentAtSlot);

                } else if (event.getAction() == InventoryAction.PLACE_ONE) {
                    ItemStack onCursorCloned = cursor.clone();
                    onCursorCloned.setAmount(1);

                    event.getClickedInventory().setItem(slot, onCursorCloned.clone());
                    onCursorCloned.setAmount(cursor.getAmount() - 1);
                    event.getWhoClicked().setItemOnCursor(onCursorCloned);

                } else if (event.getAction() == InventoryAction.PICKUP_ONE) {
                    ItemStack currentItemClone = currentAtSlot.clone();
                    currentItemClone.setAmount(1);

                    event.getWhoClicked().setItemOnCursor(currentItemClone.clone());

                    currentItemClone.setAmount(cursor.getAmount() - 1);
                    event.getClickedInventory().setItem(slot, currentItemClone);
                }
            }
            button.updateButtonFromHolder();

            ButtonClickEvent buttonClickEvent = new ButtonClickEvent(wrappedInventory, menu, event, (Player) event.getWhoClicked(), button, beforeChange, ClickEnum.match(event));
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

        SyncEvents.listen(InventoryCloseEvent.class, EventPriority.LOWEST, event -> {
            if (!(event.getInventory().getHolder() instanceof AMenu)) return;

            AMenu menu = (AMenu) event.getInventory().getHolder();
            if (menu.closeEventHandler() != null)
                menu.closeEventHandler().accept(new MenuCloseEvent(menu, event));

        });

        SyncEvents.listen(InventoryOpenEvent.class, EventPriority.LOWEST, event -> {
            if (!(event.getInventory().getHolder() instanceof AMenu)) return;

            AMenu menu = (AMenu) event.getInventory().getHolder();
            if (menu.openEventHandler() != null)
                menu.openEventHandler().accept(new MenuOpenEvent(menu, event));

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

    public void addGlobalClickHandler(String actionIdentity, Consumer<ButtonClickEvent> consumer) {
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

    public int removeFromInventory(ItemStack stack, HumanEntity player, int... slotsToIgnore) {
        int removed = 0;

        for (int slot = 0; slot < player.getInventory().getContents().length; slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (stack.getAmount() <= 0) return removed;

            boolean doContinue = false;

            for (int i : slotsToIgnore)
                if (i == slot) {
                    doContinue = true;
                }

            if (doContinue) continue;
            if (!itemStack.isSimilar(stack)) continue;

            if (itemStack.getAmount() >= stack.getAmount()) {
                if ((itemStack.getAmount() - stack.getAmount()) == 0)
                    itemStack.setAmount(0);

                else
                    itemStack.setAmount(itemStack.getAmount() - stack.getAmount());

                removed += stack.getAmount();

                stack.setAmount(0);
                return removed;

            } else {

                int canBeRemoved = stack.getAmount() - stack.getAmount();
                itemStack.setAmount(itemStack.getAmount() - canBeRemoved);

                stack.setAmount(stack.getAmount() - canBeRemoved);
                removed += canBeRemoved;

            }
        }
        return removed;
    }
}
