package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.MenuCloseEvent;
import com.oop.orangeengine.menu.events.MenuOpenEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public abstract class AMenu implements InventoryHolder {

    final private int maxSize = 54;
    @Getter
    private final String identifier;
    @Setter
    protected WrappedInventory wrappedInventory;
    @Getter
    private AMenu parent;

    @Getter
    private int size;

    @Getter
    private String title;

    @Getter
    @Setter
    private Consumer<ButtonClickEvent> globalClickHandler;

    @Getter
    @Setter
    private Consumer<MenuOpenEvent> openEventHandler;

    @Getter
    @Setter
    private Consumer<MenuCloseEvent> closeEventHandler;

    @Getter
    @Setter
    private Consumer<AMenu> updater;

    private Set<AMenu> children = new HashSet<>();

    @Getter
    private Set<AMenuButton> buttons = new LinkedHashSet<>();

    public AMenu(String identifier, int size, AMenu parent) {
        this.identifier = identifier;
        this.parent = parent;

        size(size);
    }

    public AMenu(String identifier, int size) {
        this(identifier, size, null);
    }

    private void size(int size) {
        if (size > maxSize)
            throw new IllegalStateException("Menu size is bigger than Minecraft allows (" + size + "/" + maxSize + ")");

        if (size <= 6)
            this.size = size * 9;

        else
            this.size = size;
    }

    public void title(String title) {
        this.title = title;
        //TODO Add title update packet
    }

    public boolean hasChild(String identifier) {
        return getChild(identifier, true).isPresent();
    }

    public OptionalConsumer<AMenu> getChild(String identifier, boolean deepLookup) {
        Optional<AMenu> first = children.stream()
                .filter(child -> child.identifier.equalsIgnoreCase(identifier))
                .findFirst();

        if (first.isPresent())
            return OptionalConsumer.of(first);

        else if (deepLookup) {
            return OptionalConsumer.of(children.stream()
                    .map(child -> child.getChild(identifier, true))
                    .filter(OptionalConsumer::isPresent)
                    .map(optional -> (AMenu) optional.get())
                    .findFirst());

        } else
            return OptionalConsumer.of(Optional.empty());
    }

    public boolean isSlotEmpty(int slot) {
        return buttons.stream().noneMatch(button -> button.slot() == slot);
    }

    public void addButton(AMenuButton button) {

        if (!isSlotEmpty(button.slot()))
            // Friendly warning so it's known
            Engine.getInstance().getLogger().printWarning("Duplicate slot was found in menu: " + identifier + ", slot: " + button.slot());

        buttons.add(button);
    }

    public void setButton(int slot, AMenuButton button) {
        button.slot(slot);
        buttons.add(button);
    }

    public void update() {
        if (updater != null)
            updater.accept(this);
    }

    public WrappedInventory getWrappedInventory(boolean rebuild) {
        if (rebuild || wrappedInventory == null)
            build();

        assert wrappedInventory != null;
        return wrappedInventory;
    }

    public WrappedInventory getWrappedInventory() {
        if (wrappedInventory == null)
            build();

        assert wrappedInventory != null;
        return wrappedInventory;
    }

    @Override
    public Inventory getInventory() {
        if (wrappedInventory != null)
            return wrappedInventory.getBukkitInventory();

        return null;
    }

    public AMenu parent(AMenu parent) {
        assert parent != null;

        parent.children.add(this);
        this.parent = parent;

        return this;
    }

    protected abstract void build();

    public abstract WrappedInventory getWrapperFromBukkit(Inventory inventory);

    protected abstract Inventory provideNewInv();

}
