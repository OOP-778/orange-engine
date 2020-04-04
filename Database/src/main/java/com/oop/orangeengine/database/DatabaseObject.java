package com.oop.orangeengine.database;

import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;
import com.oop.orangeengine.database.gson.MapFactory;
import com.oop.orangeengine.database.gson.RuntimeClassFactory;
import com.oop.orangeengine.database.handler.DataHandler;
import com.oop.orangeengine.database.suppliers.FieldGatherer;
import com.oop.orangeengine.database.suppliers.Suppliable;
import com.oop.orangeengine.database.util.ObjectState;
import com.oop.orangeengine.main.gson.ItemStackAdapter;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.map.OConcurrentMap;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DatabaseObject implements Suppliable {

    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeAdapterFactory(RuntimeClassFactory.of(Object.class))
            .registerTypeAdapterFactory(new MapFactory())
            .create();

    @Getter
    @Setter
    private ObjectState objectState = ObjectState.UNLOADED;

    final Map<String, DataHandler<?>> dataHandlers = new OConcurrentMap<String, DataHandler<?>>() {{
        put("default", new DataHandler<Object>() {
            @Override
            public String serialize(@Nullable Field field, Object o) throws Throwable {
                return gson.toJson(o, o.getClass());
            }

            @Override
            public Object deserialize(@Nullable Field field, String json) throws Throwable {
                return gson.fromJson(json, field != null ? field.getGenericType() : Object.class);
            }

            @Override
            public Class<Object> getClazz() {
                return Object.class;
            }
        });
    }};

    @Getter
    private final Map<String, Integer> hashCodes = new ConcurrentHashMap<>();

    @Getter
    private Set<OPair<Integer, Runnable>> runWhenLoaded = Sets.newConcurrentHashSet();

    @Getter
    private Set<OPair<Integer, Runnable>> runWhenUnloaded = Sets.newConcurrentHashSet();

    @Setter
    @Getter
    private DatabaseHolder<?, ?> holder;

    public DatabaseObject() {
    }

    public DatabaseObject(DatabaseHolder<?, ?> holder) {
        this();
        this.holder = holder;

        _loadSupplier();
    }

    public void runWhenLoaded(Runnable runnable, int priority) {
        runWhenLoaded.add(new OPair<>(priority, runnable));
    }

    public void runWhenLoaded(Runnable runnable) {
        runWhenLoaded.add(new OPair<>(2, runnable));
    }

    protected void onLoad() {
        runWhenLoaded
                .stream()
                .sorted(Comparator.comparingInt(OPair::getFirst))
                .map(OPair::getSecond)
                .forEach(Runnable::run);
        runWhenLoaded.clear();
    }

    public void save() {
        DatabaseHolder<?, DatabaseObject> holder = (DatabaseHolder<?, DatabaseObject>) this.holder;
        holder.save(this, holder);
    }

    public void save(boolean async) {
        if (holder != null) {
            DatabaseHolder<?, DatabaseObject> holder = (DatabaseHolder<?, DatabaseObject>) this.holder;
            new OTask()
                    .sync(!async)
                    .runnable(() -> holder.save(this, holder))
                    .execute();
        }
    }

    public void remove(boolean async) {
        if (holder != null) {
            DatabaseHolder<?, DatabaseObject> holder = (DatabaseHolder<?, DatabaseObject>) this.holder;
            new OTask()
                    .sync(!async)
                    .runnable(() -> holder.remove(this))
                    .execute();
        }
    }

    public <T extends Object> void registerDataHandler(String columnName, DataHandler<T> handler) {
        dataHandlers.put(columnName, handler);
    }

    public <T extends Object> void registerDataHandler(String columnName, Class<T> clazz, DataHandler<T> handler) {
        dataHandlers.put(columnName, handler);
    }

    public DataHandler<?> dataHandlerFor(Class<?> clazz) {
        Optional<DataHandler<?>> first = dataHandlers.values()
                .stream()
                .filter(dh -> dh.getClazz() != Object.class)
                .filter(dh -> dh.getClazz().isAssignableFrom(clazz))
                .findFirst();

        return first.orElse(dataHandlers.get("default"));
    }

    public DataHandler<?> dataHandlerFor(String columnName) {
        Optional<? extends DataHandler<?>> first = dataHandlers.entrySet()
                .stream()
                .filter(set -> set.getKey().toLowerCase().contentEquals(columnName.toLowerCase()))
                .map(set -> (DataHandler<?>) set.getValue())
                .findFirst();

        return first.isPresent() ? first.get() : dataHandlers.get("default");
    }

    @Override
    public void initFields(Cache<String, Field> cache) {
        FieldGatherer.create()
                .filter(field -> field.getAnnotation(Column.class) != null || field.getAnnotation(PrimaryKey.class) != null)
                .gather(getClass())
                .forEach(field -> {
                    String name = field.getAnnotation(Column.class) != null ? field.getAnnotation(Column.class).name() : field.getAnnotation(PrimaryKey.class).name();
                    cache.put(name.toLowerCase(), field);
                });
    }
}
