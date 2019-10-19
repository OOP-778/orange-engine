package com.oop.orangeengine.main.util.data.set;

import com.oop.orangeengine.main.util.data.DataModificationHandler;
import lombok.Setter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class OConcurrentSet<T> extends OSet<T> {
    private Set<T> set;

    @Setter
    private DataModificationHandler<T> handler;

    public OConcurrentSet() {
        set = ConcurrentHashMap.newKeySet();
    }

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
        if(handler != null)
            handler.onAdd(t);

        return set.add(t);
    }

    @Override
    public boolean remove(Object o) {
        if(handler != null)
            handler.onRemove((T) o);

        return set.remove(o);
    }

    public boolean removeWithoutCheck(Object o) {
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
    public Stream<T> stream() {
        return set.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return set.parallelStream();
    }
}
