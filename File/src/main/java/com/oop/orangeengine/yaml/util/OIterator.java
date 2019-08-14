package com.oop.orangeengine.yaml.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OIterator<T> {

    private T[] objects;
    private T[] objectsCopy;
    private int currentIndex = 0;

    public OIterator(List<T> objects) {

        this.objects = (T[]) objects.toArray();
        this.objectsCopy = this.objects.clone();

    }

    public OIterator(T[] objects) {
        this.objects = objects;
        this.objectsCopy = this.objects.clone();
    }

    public T next() {

        T value = objects[currentIndex];
        currentIndex++;
        if (value == null && hasNext()) return next();
        return value;

    }

    public T last() {

        return objects[currentIndex - 1];

    }

    public List<T> nextValuesThatMatches(Predicate<T> filter, boolean removeFromArray) {

        int newCurrentIndex = currentIndex;
        List<T> values = new ArrayList<>();
        List<Integer> indexesToRemove = new ArrayList<>();
        boolean done = false;

        while (!done) {

            if (objects.length - 1 < newCurrentIndex) {
                done = true;
                break;
            }
            T value = objects[newCurrentIndex];

            if (value.toString().trim().length() == 0 || value.toString().contains("#")) {

                newCurrentIndex++;
                continue;

            }

            boolean passed = filter.test(value);

            if (!passed) {
                done = true;
            } else {
                values.add(value);
                indexesToRemove.add(newCurrentIndex);
            }

            newCurrentIndex++;

        }

        if (removeFromArray) {
            indexesToRemove.forEach(index -> objects[index] = null);
        }

        return values;

    }

    public boolean hasNext() {

        int newIndex = currentIndex + 1;

        return objects.length >= newIndex;

    }

    public T[] getObjectsCopy(Class<T> type) {
        T[] array = (T[]) Array.newInstance(type, objectsCopy.length);

        for (int index = 0; index < objectsCopy.length; index++) {
            array[index] = objectsCopy[index];
        }

        return array;
    }
}
