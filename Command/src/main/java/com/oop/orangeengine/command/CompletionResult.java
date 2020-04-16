package com.oop.orangeengine.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class CompletionResult {

    private final List<Object> objects;

    public <T extends Object> Optional<T> find(Class<T> clazz) {
        return objects.stream().filter(o -> o.getClass() == clazz).findFirst().map(o -> (T) o);
    }
}
