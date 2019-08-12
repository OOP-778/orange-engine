package org.brian.core.mi;

import org.brian.core.mi.animation.MenuAnimation;
import org.brian.core.mi.button.AMenuButton;
import org.brian.core.mi.designer.MenuDesigner;
import org.brian.core.mi.events.ButtonClickEvent;
import org.brian.core.mi.events.MenuCloseEvent;
import org.brian.core.mi.events.MenuOpenEvent;
import org.brian.core.sound.OOPSound;
import org.brian.core.utils.Storagable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AMenu extends Storagable implements InventoryHolder {

    final private int maxSize = 54;
    private Consumer<MenuCloseEvent> menuCloseEvent;
    private Consumer<MenuOpenEvent> menuOpenEvent;
    private Consumer<ButtonClickEvent> globalClickEvent;
    private Consumer<AMenu> updater;
    private MenuInventory menuInventory;
    private MenuDesigner menuDesigner;
    private MenuAnimation menuAnimation;
    private AMenu parent;
    private Set<AMenuButton> buttonList = new LinkedHashSet<>();
    private Map<String, AMenu> childs = new HashMap<>();
    private String title = "Undefined Title";
    private int size;
    private String identity;
    private OOPSound clickSound;
    private boolean allowEmptySlotInteraction = false;

    public AMenu(AMenu parent, String title, int size) {
        size(size);
        title(title);
        parent(parent);
    }

    public AMenu(String title, int size) {
        this(null, title, size);
    }

    public abstract Inventory build();

    @Override
    public Inventory getInventory() {
        return menuInventory == null ? build() : menuInventory.bukkitInventory();
    }

    public AMenuButton findButtonByFilter(Predicate<AMenuButton> filter) {
        return buttonList.stream().filter(filter).findFirst().orElse(null);
    }

    public Set<AMenuButton> buttonList() {
        return buttonList;
    }

    public int size() {
        return size;
    }

    public AMenu size(int size) {

        if (size < 9) {

            if (size > 6)
                throw new IllegalStateException("The Inventory Rows Size is bigger than the max " + size + " > " + 6);
            size = size * 9;

        } else if (size >= maxSize)
            throw new IllegalStateException("The Inventory Size is bigger than the max " + size + " > " + 54);

        this.size = size;
        return this;
    }

    public String title() {
        return title;
    }

    public AMenu menuDesigner(MenuDesigner designer) {
        this.menuDesigner = designer;
        return this;
    }

    public MenuDesigner menuDesigner() {
        return menuDesigner;
    }

    public MenuAnimation menuAnimation() {
        return menuAnimation;
    }

    public AMenu menuAnimation(MenuAnimation menuAnimation) {
        this.menuAnimation = menuAnimation;
        return this;
    }

    public AMenu title(String title) {
        this.title = title;
        return this;
    }

    public Map<String, AMenu> childs() {
        return childs;
    }

    public AMenu parent() {
        return parent;
    }

    public AMenu parent(AMenu parent) {
        this.parent = parent;
        return this;
    }

    public AMenu mainMenu() {
        if (parent() == null) return this;
        else return parent.mainMenu();
    }

    public Map<String, AMenu> allMenus() {

        Map<String, AMenu> pages = new HashMap<>();
        AMenu currentMenu = this;

        if (currentMenu.parent() == null)
            return currentMenu.childs();

        while (currentMenu.parent() != null) {

            pages.putAll(currentMenu.childs);

            currentMenu = currentMenu.parent();

        }

        pages.putAll(currentMenu.childs);
        return pages;

    }

    public AMenu addChild(String childName, AMenu menu) {
        menu.parent(this);
        childs.put(childName, menu);
        return this;
    }

    public AMenu addButton(AMenuButton button) {
        if (button == null) throw new NullPointerException("Failed to add button to menu, because button is null.");
        buttonList.add(button);
        return this;
    }

    public MenuInventory menuInventory() {

        if (menuInventory == null) build();
        return menuInventory;

    }

    public AMenu menuInventory(MenuInventory menuInventory) {
        this.menuInventory = menuInventory;
        return this;
    }

    public AMenu setDummies(Collection<AMenuButton> buttons, MenuInventory inventory) {

        buttons.forEach(inventory::addButton);
        return this;

    }

    public AMenu closeEvent(Consumer<MenuCloseEvent> menuCloseEvent) {
        this.menuCloseEvent = menuCloseEvent;
        return this;
    }

    public Consumer<MenuCloseEvent> closeEvent() {
        return this.menuCloseEvent;
    }

    public AMenu openEvent(Consumer<MenuOpenEvent> menuOpenEvent) {
        this.menuOpenEvent = menuOpenEvent;
        return this;
    }

    public Consumer<MenuOpenEvent> openEvent() {
        return this.menuOpenEvent;
    }

    public AMenu child(String childName) {
        return childs().get(childName);
    }

    public Inventory bukkitInventory(boolean rebuild) {
        if (rebuild) return build();
        return menuInventory() == null ? build() : menuInventory().bukkitInventory();
    }

    public Inventory bukkitInventory() {
        return bukkitInventory(false);
    }

    public AMenu updater(Consumer<AMenu> updater) {
        this.updater = updater;
        return this;
    }

    public Consumer<AMenu> updater() {
        return updater;
    }

    public void update() {

        if (updater == null) return;
        updater.accept(this);

    }

    public AMenu globalClickEvent(Consumer<ButtonClickEvent> globalClickEvent) {
        this.globalClickEvent = globalClickEvent;
        return this;
    }

    public Consumer<ButtonClickEvent> globalClickEvent() {
        return globalClickEvent;
    }

    public boolean isMenuInvPresent() {
        return menuInventory != null;
    }

    public AMenu replaceButton(AMenuButton button, AMenuButton replace) {

        replace.slot(button.slot());
        buttonList.remove(button);
        buttonList.add(replace);

        return this;

    }

    public String identity() {
        return identity;
    }

    public AMenu identity(String identity) {
        this.identity = identity;
        return this;
    }

    public OOPSound clickSound() {
        return clickSound;
    }

    public AMenu clickSound(OOPSound clickSound) {
        this.clickSound = clickSound;
        return this;
    }

    public boolean isAllowEmptySlotInteraction() {
        return allowEmptySlotInteraction;
    }

    public AMenu allowEmptySlotInteraction(boolean allow) {
        this.allowEmptySlotInteraction = allow;
        return this;
    }

}
