package org.brian.core.mi;

import org.brian.core.OOPCore;
import org.brian.core.events.SyncEvents;
import org.brian.core.logger.OOPLogger;
import org.brian.core.mi.button.AMenuButton;
import org.brian.core.mi.events.ButtonClickEvent;
import org.brian.core.mi.events.MenuCloseEvent;
import org.brian.core.mi.events.MenuOpenEvent;
import org.brian.core.mi.types.PagedMenu;
import org.brian.core.task.OOPTask;
import org.brian.core.utils.InventoryUtil;
import org.brian.core.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MenuListener {

    public MenuListener() {

        SyncEvents.listen(InventoryClickEvent.class, event -> {

            //First we check if clicked inventory is not AMenu and if slot is not valid.
            if (Integer.toString(event.getSlot()).contains("-")) return;
            if (!(event.getClickedInventory().getHolder() instanceof AMenu)) return;

            AMenu clickedMenu = ((AMenu) event.getClickedInventory().getHolder());

            //Then we check for clickedItem
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                if (!clickedMenu.isAllowEmptySlotInteraction())
                    event.setCancelled(true);
                return;
            }

            try {
                AMenuButton button;
                MenuInventory menuInventory;
                if (clickedMenu instanceof PagedMenu) {
                    button = ((PagedMenu) clickedMenu).findButtonBySlot(event.getClickedInventory(), event.getSlot());
                    menuInventory = ((PagedMenu) clickedMenu).bukkitToMI(event.getInventory());
                } else {
                    button = clickedMenu.findButtonByFilter(b -> b.slot() == event.getSlot());
                    menuInventory = clickedMenu.menuInventory();
                }

                if (button != null) {

                    ButtonClickEvent buttonClickEvent = new ButtonClickEvent(menuInventory, button, ((Player) event.getWhoClicked()), event);
                    Bukkit.getPluginManager().callEvent(buttonClickEvent);

                    if (!buttonClickEvent.isCancelled()) {

                        if (button.clickEvent() != null) button.clickEvent().accept(buttonClickEvent);
                        if (button.isCancelEvent()) event.setCancelled(true);
                        if (button.clickSound() != null)
                            button.clickSound().play(((Player) event.getWhoClicked()), 1f, 1f);

                        if (clickedMenu.globalClickEvent() != null)
                            clickedMenu.globalClickEvent().accept(buttonClickEvent);

                    } else event.setCancelled(true);
                }
            } catch (Exception ex) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cError! Report this to Administrators! (" + ex.getMessage() + ")"));
                OOPLogger.findOrDefault().error(ex);

            }

        }, OOPCore.instance());

        SyncEvents.listen(InventoryCloseEvent.class, event -> {

            if (event.getInventory().getHolder() instanceof AMenu) {

                boolean isFinal = event.getInventory().getViewers().size() <= 1;
                AMenu menu = (AMenu) event.getInventory().getHolder();
                MenuInventory menuInventory;
                if (menu instanceof PagedMenu) menuInventory = ((PagedMenu) menu).bukkitToMI(event.getInventory());
                else menuInventory = menu.menuInventory();

                MenuCloseEvent closeEvent = new MenuCloseEvent(menuInventory, event, isFinal);
                if (closeEvent.isCancelled()) {
                    event.getPlayer().openInventory(menuInventory.bukkitInventory());
                } else if (menu.closeEvent() != null) menu.closeEvent().accept(closeEvent);

                checkForDupe(menuInventory, ((Player) event.getPlayer()));

            }

        }, OOPCore.instance());
        SyncEvents.listen(InventoryOpenEvent.class, event -> {

            if (event.getInventory().getHolder() instanceof AMenu) {

                AMenu menu = (AMenu) event.getInventory().getHolder();
                MenuInventory menuInventory;
                if (menu instanceof PagedMenu) menuInventory = ((PagedMenu) menu).bukkitToMI(event.getInventory());
                else menuInventory = menu.menuInventory();

                MenuOpenEvent openEvent = new MenuOpenEvent(menuInventory, event);
                if (openEvent.isCancelled()) {
                    event.setCancelled(true);
                } else if (menu.openEvent() != null) menu.openEvent().accept(openEvent);

            }

        }, OOPCore.instance());
    }

    private void checkForDupe(MenuInventory menuInventory, Player player) {

        new OOPTask().
                sync(false).
                delay(300).
                runnable(() -> {
                    player.updateInventory();

                    List<AMenuButton> buttonsToCheck = menuInventory.buttons().stream().filter(b -> b != null && b.isCancelEvent()  && !b.ignoreDupeCheck()).collect(Collectors.toList());
                    Inventory playerInventory = player.getInventory();

                    List<ItemStack> toFilter = buttonsToCheck.stream().map(AMenuButton::itemStack).collect(Collectors.toList());

                    InventoryUtil.getItemsFilteredByItem(playerInventory, item -> toFilter.stream().anyMatch(bi -> bi.isSimilar(item))).forEach((slot, item) -> {
                        playerInventory.setItem(slot, new ItemStack(Material.AIR));
                    });
                }).
                run();

    }

}
