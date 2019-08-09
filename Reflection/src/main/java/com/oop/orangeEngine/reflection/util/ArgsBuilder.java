package com.oop.orangeEngine.reflection.util;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class ArgsBuilder {

    private Set<Object[]> args = new HashSet<>();

    public ArgsBuilder with(Object... arg) {
        this.args.add(arg);
        return this;
    }

    public Set<Object[]> build() {
        return args;
    }
}
