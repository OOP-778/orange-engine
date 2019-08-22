package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.MenuCloseEvent;
import com.oop.orangeengine.menu.events.MenuOpenEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public abstract class AMenu {

    final private int maxSize = 54;

    @Setter
    private WrappedInventory wrappedInventory;

    @Getter
    private final String identifier;

    @Getter
    private AMenu parent;

    @Getter
    private int size;

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

    private void size(int size) {
        if(size > maxSize)
            throw new IllegalStateException("Menu size is bigger than Minecraft allows (" + size + "/" + maxSize + ")");

        if (size <= 6)
            this.size = size * 9;

        else
            this.size = size;
    }

    public AMenu(String identifier, int size) {
        this(identifier, size, null);
    }

    public boolean hasChild(String identifier, boolean deepLookup) {

        Optional<AMenu> first = children.stream()
                .filter(child -> child.identifier.equalsIgnoreCase(identifier))
                .findFirst();

        if (first.isPresent())
            return true;

        else if (deepLookup) {
            return children.stream()
                    .anyMatch(child -> child.hasChild(identifier, true));

        } else
            return false;
    }

    public boolean isSlotEmpty(int slot) {
        return buttons.stream().noneMatch(button -> button.slot() == slot);
    }

    public void addButton(AMenuButton button) {

        if (!isSlotEmpty(button.slot()))
            //Friendly warning so it's known
            Engine.getInstance().getLogger().printWarning("Duplicate slot found in menu: " + identifier + ", slot: " + button.slot());

        buttons.add(button);
    }

    public void update() {
        if(updater != null)
            updater.accept(this);
    }

    public WrappedInventory getInventory(boolean rebuild) {

        if(rebuild || wrappedInventory == null)
            build();

        assert wrappedInventory != null;
        return wrappedInventory;

    }

    public WrappedInventory getInventory() {

        if(wrappedInventory == null)
            build();

        assert wrappedInventory != null;
        return wrappedInventory;

    }

    public AMenu parent(AMenu parent) {
        assert parent != null;

        parent.children.add(this);
        this.parent = parent;

        return this;
    }

    protected abstract void build();

}
