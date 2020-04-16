package com.oop.orangeengine.message;

import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Contentable extends Cloneable {
    default <T> List<String> replaceList(@NonNull T object, Collection<String> multiLine, @NonNull Set<OPair<String, Function<T, String>>> placeholders) {
        return multiLine
                .stream()
                .map(line -> replaceText(object, line, placeholders))
                .collect(Collectors.toList());
    }

    default <T> String apply(@NonNull T object, @NonNull String text, @NonNull Set<OPair<String, Function<T, String>>> placeholders) {
        String[] array = new String[]{text};
        placeholders.forEach(placeholder -> array[0] = array[0].replace(Objects.requireNonNull(placeholder.getFirst(), "Placeholder identifier cannot be null"), toString(Objects.requireNonNull(placeholder.getSecond(), "Placeholder handler cannot be null").apply(object))));
        return array[0];
    }

    default String toString(Object object) {
        return object == null ? "" : object.toString();
    }

    default <T> String replaceText(@NonNull T object, @NonNull String text, @NonNull Set<OPair<String, Function<T, String>>> placeholders) {
        String[] array = new String[]{text};
        array[0] = apply(object, array[0], placeholders);
        return array[0];
    }

    <T> void replace(T object, Set<OPair<String, Function<T, String>>> placeholders);

    void replace(Map<String, Object> placeholders);

    Contentable clone();
}
