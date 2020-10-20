package com.oop.orangeengine.main.util.data.cache;

import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class OCache<K, V> {

    @Getter
    private final int concurrencyLevel;

    private final long expireAfter;

    private final boolean resetExpireAfterAccess;

    // Key, Value with when it expires
    private final Map<K, OPair<V, Long>> data;

    OCache(Builder builder) {
        this.concurrencyLevel = builder.concurrencyLevel;
        this.expireAfter = builder.expireAfter;
        this.resetExpireAfterAccess = builder.resetExpireAfterAccess;

        if (concurrencyLevel == 0)
            data = new HashMap<>();
        else
            data = new ConcurrentHashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public V get(K key) {
        checkForInvalids();

        OPair<V, Long> valuePair = data.get(key);
        if (valuePair != null && resetExpireAfterAccess)
            valuePair.setSecond(System.currentTimeMillis() + expireAfter);

        return valuePair == null ? null : valuePair.getFirst();
    }

    public void clear() {
        data.clear();
    }

    public void remove(K key) {
        data.remove(key);
    }

    public void put(K key, V value) {
        checkForInvalids();

        data.remove(key);
        data.put(key, new OPair<>(value, System.currentTimeMillis() + expireAfter));
    }

    public V getIfAbsent(K key, Supplier<V> supplier) {
        V value = get(key);
        if (value == null) {
            value = supplier.get();
            put(key, value);
        }

        return value;
    }

    private void checkForInvalids() {
        if (expireAfter == -1) return;
        data.forEach((key, value) -> {
            if (value.getSecond() <= System.currentTimeMillis())
                remove(key);
        });
    }

    @Accessors(chain = true, fluent = true)
    public static class Builder {
        // Level 0 = Sync Use Only, level 1 = Full Multi Thread support
        @Setter
        private int concurrencyLevel = 0;

        @Setter
        private boolean resetExpireAfterAccess = false;

        // How long will the values be in map
        @Setter
        private long expireAfter = -1;

        public Builder() {
        }

        public Builder expireAfter(long time, TimeUnit unit) {
            this.expireAfter = unit.toMillis(time);
            return this;
        }

        public <K, V> OCache<K, V> build() {
            return new OCache<>(this);
        }
    }

    public Set<K> keySet() {
        checkForInvalids();
        return data.keySet();
    }
}
