package com.oop.orangeengine.menu.newVersion.button.attribute;

import com.oop.orangeengine.menu.ButtonHolder;
import com.oop.orangeengine.menu.newVersion.button.MenuButton;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ButtonAttributes implements Cloneable {
    private Map<String, Object> data = new ConcurrentHashMap<>();

    @Getter
    private MenuButton button;

    public ButtonAttributes(MenuButton button) {
        this.button = button;
    }

    public <T extends Object> ButtonAttributes add(String attribute, T object) {
        data.remove(attribute);
        data.put(attribute, object);
        return this;
    }

    public <T extends Object> Optional<T> get(String attribute, Class<T> type) {
        return Optional.ofNullable((T) data.get(attribute));
    }

    public <T extends Object> Optional<T> get(String attribute) {
        return get(attribute, null);
    }

    @SneakyThrows
    public ButtonAttributes clone() {
        return (ButtonAttributes) super.clone();
    }

    public void assignButton(MenuButton button) {
        this.button = button;
    }
}
