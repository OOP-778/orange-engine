package com.oop.orangeengine.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Getter
public class ResultCache {

    private final List<Object> objects = new LinkedList<>();

    public <T extends Object> Optional<T> find(Class<T> clazz) {
        return objects.stream().filter(o -> clazz.isAssignableFrom(o.getClass())).findFirst().map(o -> (T) o);
    }
}
