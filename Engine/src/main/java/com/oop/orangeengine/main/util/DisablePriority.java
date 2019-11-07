package com.oop.orangeengine.main.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public enum DisablePriority {

    LAST(3),
    MIDDLE(2),
    FIRST(1);

    private Integer order;
    DisablePriority(int order) {
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }
}
