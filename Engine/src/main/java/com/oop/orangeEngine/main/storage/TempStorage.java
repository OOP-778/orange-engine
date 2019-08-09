package com.oop.orangeEngine.main.storage;

import com.google.common.collect.HashBiMap;
import com.oop.orangeEngine.main.task.OTask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TempStorage<K, V> extends ConcurrentHashMap<K, V> {

    private HashBiMap<K, Long> lastUsed = HashBiMap.create();
    private OTask updateTask;
    private long expireTime = -1;

    public TempStorage() {
        super();

        updateTask = new OTask().
                delay(TimeUnit.SECONDS, 1).
                sync(false).
                stopIf(task -> isEmpty()).
                runnable(this::update).
                execute();


    }

    public void update() {
        if (expireTime == -1) return;
        long currentTime = System.currentTimeMillis();

        Set<Long> times = new HashSet<>(lastUsed.values());
        times.forEach(time -> {
            if ((currentTime - time) >= expireTime) {
                K key = lastUsed.inverse().get(time);
                this.remove(key);
                lastUsed.remove(key);

            }

        });

    }

    @Override
    public V put(K k, V v) {
        super.put(k, v);

        V v2 = get(k);
        if (v2 != null && v2 == v)
            lastUsed.put(k, System.currentTimeMillis());

        return v;

    }

    @Override
    public V remove(Object o) {
        V v = super.remove(o);
        lastUsed.remove(o);
        return v;
    }

    @Override
    public V get(Object o) {
        V value = super.get(o);

        K key = (K) o;
        lastUsed.remove(key);
        lastUsed.put(key, System.currentTimeMillis());

        return value;

    }

    public TempStorage<K, V> setExpireTime(long time, TimeUnit unit) {
        this.expireTime = unit.toMillis(time);
        return this;
    }

    public TempStorage<K, V> setExpireTime(long time) {
        return setExpireTime(time, TimeUnit.MILLISECONDS);
    }

}
