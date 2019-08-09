package com.oop.orangeEngine.main.util;


import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;

public class OQueue<T> extends AbstractQueue<T> {

    private LinkedList<T> elements = new LinkedList<>();

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean offer(T T) {
        if (T == null) return false;
        elements.add(T);
        return true;
    }

    @Override
    public T poll() {
        Iterator<T> iter = elements.iterator();
        T t = iter.next();
        if (t != null) {
            iter.remove();
            return t;
        }
        return null;
    }

    @Override
    public T peek() {
        return elements.getFirst();
    }
}
