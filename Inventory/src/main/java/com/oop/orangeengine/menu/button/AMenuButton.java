package com.oop.orangeengine.menu.button;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.storage.Storegable;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.map.OMap;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.impl.BukkitItem;
import com.oop.orangeengine.menu.button.impl.SwappableButton;
import com.oop.orangeengine.menu.config.action.ActionTypesController;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.sound.WrappedSound;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@EqualsAndHashCode
@Accessors(fluent = true, chain = true)
public abstract class AMenuButton extends Storegable implements Cloneable {

    @Getter
    private Set<ClickListener> clickListeners = new HashSet<>();

    @Getter
    private Set<String> appliedActions = new HashSet<>();

    @Getter
    private ItemStack currentItem;

    @Getter
    private int slot = -1;

    @Setter
    @Getter
    private WrappedInventory holder;

    @Setter
    @Getter
    private WrappedSound sound;

    @Setter
    @Getter
    private boolean pickable = false;

    @Getter
    @Setter
    private boolean paged = false;

    @Getter
    @Setter
    private boolean placeholder = false;

    @Getter
    @Setter
    private boolean template = false;

    @Getter
    @Setter
    private boolean actAsFilled = false;

    public AMenuButton(ItemStack currentItem, int slot) {
        this.currentItem = currentItem;
        this.slot = slot;
    }

    public <T extends ButtonClickEvent> AMenuButton clickHandler(ClickEnum clickEnum, Class<T> type, Consumer<T> event) {
        ClickListener<T> clickListener = new ClickListener<>(type);
        clickListener.clickEnum(clickEnum);
        clickListener.consumer(event);

        clickListeners.add(clickListener);
        return this;
    }

    public <T extends ButtonClickEvent> AMenuButton clickHandler(Class<T> type, Consumer<ButtonClickEvent> event) {
        ClickListener<T> clickListener = new ClickListener<>(type);
        clickListener.clickEnum(ClickEnum.GLOBAL);
        clickListener.consumer(event);

        clickListeners.add(clickListener);
        return this;
    }

    public void remove() {
        if (holder != null)
            holder.setButton(slot, BukkitItem.newAir(slot));
    }

    public AMenuButton currentItem(ItemStack itemStack, boolean update) {
        currentItem = itemStack;
        if (holder != null && update)
            holder.updateButton(this);

        return this;
    }

    public AMenuButton currentItem(ItemStack itemStack) {
        return currentItem(itemStack, true);
    }

    public AMenuButton updateButtonFromHolder() {
        if (holder != null && holder.getBukkitInventory() != null) {
            currentItem = holder.getBukkitInventory().getItem(slot);
            if (currentItem == null)
                currentItem = new ItemStack(Material.AIR);
        }

        return this;
    }

    public AMenuButton slot(int slot) {
        if (holder != null)
            holder.setButton(this.slot, null);

        this.slot = slot;
        return this;
    }

    public AMenuButton slotNoUpdate(int slot) {
        this.slot = slot;
        return this;
    }

    @Override
    public AMenuButton clone() {
        AMenuButton aMenuButton = null;
        try {
            aMenuButton = (AMenuButton) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assert aMenuButton != null;

        aMenuButton.holder = null;
        aMenuButton.currentItem = currentItem.clone();

        return aMenuButton;
    }

    public <T extends AMenuButton> T cast() {
        return (T) this;
    }

    public <T extends AMenuButton> T cast(Class<T> klass) {
        return (T) this;
    }

    public void setAmount(int amount) {
        currentItem.setAmount(amount);
        currentItem(currentItem);
    }

    public int getAmount() {
        return currentItem.getAmount();
    }

    public AMenuButton addClickHandler(ClickListener listener) {
        clickListeners.add(listener);
        return this;
    }

    public AMenuButton saveCurrentItem(String id) {
        putIfPresentReplace(id, currentItem.clone());
        return this;
    }

    public OptionalConsumer<ItemStack> getSavedCopy(String id) {
        return grab(id);
    }

    public static SwappableButton newNextPageButton(ItemStack item) {
        SwappableButton nextPage = new SwappableButton(item);
        nextPage.addClickHandler(new ClickListener<ButtonClickEvent>(ButtonClickEvent.class).consumer(ActionTypesController.getActionTypes().get("execute action").apply("next page")));
        nextPage.appliedActions().add("next page");
        return nextPage;
    }

    public static SwappableButton newLastPageButton(ItemStack item) {
        SwappableButton nextPage = new SwappableButton(item);
        nextPage.addClickHandler(new ClickListener<ButtonClickEvent>(ButtonClickEvent.class).consumer(ActionTypesController.getActionTypes().get("execute action").apply("last page")));
        nextPage.appliedActions().add("last page");
        return nextPage;
    }
}
