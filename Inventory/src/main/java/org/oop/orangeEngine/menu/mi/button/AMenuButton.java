package org.brian.core.mi.button;

import lombok.NonNull;
import org.brian.core.mi.MenuInventory;
import org.brian.core.mi.events.ButtonClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AMenuButton extends Storagable implements Cloneable {

    private ItemStack itemStack;
    private List<MenuInventory> holders = new ArrayList<>();
    private int slot;
    private String identity;
    private String actionIdentity = "";
    private Consumer<ButtonClickEvent> clickEvent;
    private OOPSound clickSound;
    private ItemStack replacement;

    private boolean isFiller = false;
    private boolean pagedButton = false;
    private boolean isTempButton = false;
    private boolean cancelEvent = true;
    private boolean ignoreDupeCheck = false;

    public AMenuButton(ItemStack itemStack, int slot, String identity, Consumer<ButtonClickEvent> clickEvent, OOPSound clickSound) {

        this.itemStack = itemStack;
        this.slot = slot;
        this.identity = identity;
        this.clickEvent = clickEvent;
        this.clickSound = clickSound;

    }

    public AMenuButton(ItemStack itemStack, int slot, String identifier, Consumer<ButtonClickEvent> clickEvent) {
        this(itemStack, slot, identifier, clickEvent, null);
    }

    public AMenuButton(ItemStack itemStack, int slot, String identifier) {
        this(itemStack, slot, identifier, null);
    }

    public AMenuButton(ItemStack itemStack, int slot) {
        this(itemStack, slot, null);
    }

    public AMenuButton(ItemStack itemStack) {
        this(itemStack, -1);
    }

    public void saveItemCopy(String saveName) {
        putData(saveName, itemStack().clone());
    }

    public void saveItemCopy() {
        saveItemCopy("defaultCopy");
    }

    public ItemStack itemCopy(String copyName) {
        return containsData(copyName) ? getData(copyName) : null;
    }

    public ItemStack itemCopy() {
        return itemCopy("defaultCopy");
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    public List<MenuInventory> holders() {
        return holders;
    }

    public int slot() {
        return slot;
    }

    public Consumer<ButtonClickEvent> clickEvent() {
        return clickEvent;
    }

    public OOPSound clickSound() {
        return clickSound;
    }

    public String identity() {
        return identity == null ? "undefined" : identity;
    }

    public AMenuButton clickEvent(Consumer<ButtonClickEvent> clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public AMenuButton slot(int slot) {
        this.slot = slot;
        return this;
    }

    public AMenuButton clickSound(OOPSound clickSound) {
        this.clickSound = clickSound;
        return this;
    }

    public AMenuButton identity(String identity) {
        this.identity = identity;
        return this;
    }

    public AMenuButton addHolder(MenuInventory inventory) {
        holders.add(inventory);
        return this;
    }

    public boolean isFiller() {
        return isFiller;
    }

    public AMenuButton isFiller(boolean filler) {
        isFiller = filler;
        return this;
    }

    public boolean isPagedButton() {
        return pagedButton;
    }

    public AMenuButton isPagedButton(boolean isPagedButton) {
        this.pagedButton = isPagedButton;
        return this;
    }

    @Override
    public AMenuButton clone() {
        try {
            AMenuButton clone = (AMenuButton) super.clone();

            clone.itemStack = itemStack.clone();
            if (replacement != null) clone.replacement(replacement.clone());
            clone.holders = new ArrayList<>();

            return clone;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean isTempButton() {
        return isTempButton;
    }

    public AMenuButton isTempButton(boolean isTempButton) {
        this.isTempButton = isTempButton;
        return this;
    }

    public boolean isCancelEvent() {
        return cancelEvent;
    }

    public AMenuButton cancelEvent(boolean cancelEvent) {
        this.cancelEvent = cancelEvent;
        return this;
    }

    public AMenuButton replacement(ItemStack itemStack) {
        this.replacement = itemStack;
        return this;
    }

    public ItemStack replacement() {
        return replacement;
    }

    public AMenuButton replace() {

        if (replacement() != null) {

            saveItemCopy();
            changeItemStack(replacement());

        }

        return this;

    }

    public AMenuButton replace(@NonNull AMenuButton replaceButton) {

        holders().forEach(mi -> {
            mi.replaceButton(this, replaceButton);
        });

        return this;

    }

    public AMenuButton replace(ItemStack item) {

        saveItemCopy();
        changeItemStack(item);

        return this;

    }


    public AMenuButton switchWithReplacement() {

        if (replacement() != null) {

            saveItemCopy();
            changeItemStack(replacement());
            replacement(itemCopy());

        }

        return this;

    }

    public AMenuButton changeItemStack(ItemStack itemStack) {

        this.itemStack = itemStack;
        holders().forEach(mi -> {
            mi.updateButton(this);
        });

        return this;

    }

    public String actionIdentity() {
        return actionIdentity;
    }

    public AMenuButton actionIdentity(String actionIdentity) {
        this.actionIdentity = actionIdentity;
        return this;
    }

    public boolean ignoreDupeCheck() {
        return ignoreDupeCheck;
    }

    public void ignoreDupeCheck(boolean ignoreDupeCheck) {
        this.ignoreDupeCheck = ignoreDupeCheck;
    }
}
