package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.util.data.OQueue;
import com.oop.orangeengine.main.util.data.map.OMap;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.menu.packet.PacketUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryUtil {

    public static int addItem(ItemStack itemStack, Player player) {
        return addItem(itemStack, itemStack.getAmount(), player.getInventory());
    }

    public static int addItem(ItemStack itemStack, int amount, Player player) {
        return addItem(itemStack, amount, player.getInventory());
    }

    public static int addItem(ItemStack itemStack, int amount, Inventory inventory) {
        Set<ItemStack> itemStacks = new HashSet<>();
        ItemStack clone;

        while (amount > 0) {
            clone = itemStack.clone();
            if (amount > 64) {
                clone.setAmount(64);
                itemStacks.add(clone);

                amount -= 64;
            } else {
                clone.setAmount(amount);
                itemStacks.add(clone);

                amount = 0;
            }
        }

        return addItem(itemStacks, inventory);
    }

    public static int addItem(Set<ItemStack> itemStacks, Inventory inventory) {
        OQueue<Integer> emptySlots = new OQueue<>();
        int added = 0;
        List<OPair<ItemStack, Integer>> matchedItems = new ArrayList<>();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack atSlot = inventory.getContents()[slot];
            if (atSlot == null || atSlot.getType() == Material.AIR)
                emptySlots.offer(slot);

            else if (itemStacks.stream().filter(item2 -> item2.getAmount() < item2.getMaxStackSize()).anyMatch(item2 -> item2.isSimilar(atSlot)))
                matchedItems.add(new OPair<>(atSlot, slot));
        }

        OMap<Integer, ItemStack> changedSlots = new OMap<>();
        for (ItemStack item1 : itemStacks) {
            for (OPair<ItemStack, Integer> item2 : matchedItems) {
                if (!item1.isSimilar(item2.getFirst())) continue;
                if (item1.getAmount() == 0) continue;

                int canFit = item2.getFirst().getMaxStackSize() - item2.getFirst().getAmount();
                if (canFit < item1.getAmount()) {
                    added += canFit;
                    item1.setAmount(item1.getAmount() - canFit);
                    item2.getFirst().setAmount(item2.getFirst().getAmount() + canFit);
                    changedSlots.putIfPresentReplace(item2.getSecond(), item2.getFirst());

                } else {
                    added += item1.getAmount();
                    item2.getFirst().setAmount(item1.getAmount());
                    item1.setAmount(0);

                    changedSlots.putIfPresentReplace(item2.getSecond(), item2.getFirst());
                }
            }

            if (!emptySlots.isEmpty()) {
                int firstEmpty = emptySlots.peek();

                changedSlots.put(firstEmpty, item1.clone());
                added += item1.getAmount();
                item1.setAmount(0);
            }

        }

        List<Player> viewers = inventory.getViewers().stream()
                .map(human -> (Player)human)
                .collect(Collectors.toList());

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = changedSlots.get(slot);
            if (itemStack == null) continue;

            int finalSlot = slot;
            viewers.forEach(player -> PacketUtils.updateSlot(player, finalSlot, itemStack, false));
            inventory.setItem(slot, itemStack);

        }

        return added;
    }

    public static void updateTitle(Inventory inventory, String title) {
        inventory.getViewers().forEach(viewier -> PacketUtils.updateTitle((Player) viewier, inventory, title));
    }

}
