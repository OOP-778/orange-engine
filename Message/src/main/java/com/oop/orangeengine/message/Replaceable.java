package com.oop.orangeengine.message;

import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.ChatLine;
import com.oop.orangeengine.message.impl.chat.LineContent;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface Replaceable<T extends Replaceable> {
    default T replace(@NonNull String key, @NonNull Object value) {
        if (value instanceof LineContent) {
            if (this instanceof OChatMessage)
                ((OChatMessage) this).replace(key, (LineContent) value);
            else if (this instanceof ChatLine)
                ((ChatLine) this).replace(key, (LineContent) value);
            else
                replace(ImmutableMap.of(key, ((LineContent) value).text()));
        } else if (value instanceof OChatMessage) {
            if (this instanceof OChatMessage)
                ((OChatMessage) this).replace(key, (OChatMessage) value);

            else if (this instanceof ChatLine)
                ((ChatLine) this).replace(key, (OChatMessage) value);

        } else if (value instanceof ChatLine) {
            if (this instanceof OChatMessage)
                ((OChatMessage) this).replace(key, (ChatLine) value);

            else if (this instanceof ChatLine)
                ((ChatLine) this).replace(key, (ChatLine) value);
        } else
            replace(ImmutableMap.of(key, value));

        return returnThis();
    }

    T replace(Map<String, Object> placeholders);

    <E> T replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders);

    T replace(@NonNull Function<String, String> function);

    T returnThis();
}
