package com.oop.orangeengine.message.impl.chat.addition;

public interface Parentable<T> {

    T parent();

    void parent(T parent);

}
