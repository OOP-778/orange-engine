package com.oop.orangeengine.hologram;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

public class SetWrapper<T> implements Set<T> {

    private Set<T> set = Sets.newConcurrentHashSet();

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return set.toArray(t1s);
    }

    @Override
    public boolean add(T t) {
        return set.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return set.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return set.addAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return set.retainAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return set.removeAll(collection);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean equals(Object o) {
        return set.equals(o);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    public Optional<T> get(int index) {
        return stream().skip(index).findFirst();
    }

    public void insert(int index, T obj, InsertionRule rule) {
        Set<T> newSet = Sets.newConcurrentHashSet();

        int i = 0;
        for (T obj2 : this) {
            if (i == index) {
                if (rule == InsertionRule.AFTER) {
                    newSet.add(obj2);
                    newSet.add(obj);

                } else {
                    newSet.add(obj);
                    newSet.add(obj2);
                }
            }
            i++;
        }

        this.set = newSet;
    }

    public void set(int index, T obj) {
        if (index > size())
            add(obj);

        else {
            Set<T> newSet = Sets.newConcurrentHashSet();
            int i = 0;
            for (T obj2 : this) {
                if (i == index)
                    newSet.add(obj);

                else
                    newSet.add(obj2);

                i++;
            }
            this.set = newSet;
        }
    }

    public void insert(int index, T obj) {
        insert(index, obj, InsertionRule.AFTER);
    }

    public static enum InsertionRule {
        BEFORE,
        AFTER
    }
}
