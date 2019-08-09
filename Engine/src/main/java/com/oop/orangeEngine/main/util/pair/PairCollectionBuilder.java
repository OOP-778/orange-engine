package com.oop.orangeEngine.main.util.pair;

import java.util.Collection;
import java.util.function.Supplier;

public class PairCollectionBuilder<T extends IPair> {

    private Collection<T> collection;

    public PairCollectionBuilder(Supplier<Collection<T>> collectionSupplier) {
        this.collection = collectionSupplier.get();
    }

    public PairCollectionBuilder<T> with(T pair) {
        collection.add(pair);
        return this;
    }

    public Collection<T> build() {
        return collection;
    }

    public <C> C build(Class<C> collType) {
        return (C) collection;
    }

}
