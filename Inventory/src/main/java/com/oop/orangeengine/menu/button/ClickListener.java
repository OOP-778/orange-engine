package com.oop.orangeengine.menu.button;

import com.oop.orangeengine.menu.events.ButtonClickEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ClickListener<T extends ButtonClickEvent> {

    private boolean shiftRequired = false;
    private Consumer<? extends ButtonClickEvent> consumer;
    private final Class<T> type;
    private ClickEnum clickEnum = ClickEnum.GLOBAL;

    public ClickListener(Class<T> type) {
        this.type = type;
    }

}
