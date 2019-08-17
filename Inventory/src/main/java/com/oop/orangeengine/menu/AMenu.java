package com.oop.orangeengine.menu;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.MenuCloseEvent;
import com.oop.orangeengine.menu.events.MenuOpenEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class AMenu {

    final private int maxSize = 54;

    @Getter @Setter
    private String identifier;

    @Getter @Setter
    private AMenu parent;

    @Getter @Setter
    private Consumer<ButtonClickEvent> globalClickHandler;

    @Getter @Setter
    private Consumer<MenuOpenEvent> openEventHandler;

    @Getter @Setter
    private Consumer<MenuCloseEvent> closeEventHandler;

    @Getter @Setter
    private Consumer<AMenu> updater;

    private Set<AMenu> children = new HashSet<>();

    @Getter
    private Set<AMenuButton> buttons = new LinkedHashSet<>();

    public boolean hasChild(String identifier, boolean deepLookup) {
        Optional<AMenu> first = children.stream()
                .filter(child -> child.identifier.equalsIgnoreCase(identifier))
                .findFirst();

        if(first.isPresent())
            return true;

        else if(deepLookup) {
            return children.stream()
                    .anyMatch(child -> child.hasChild(identifier, true));

        } else
            return false;
    }

}
