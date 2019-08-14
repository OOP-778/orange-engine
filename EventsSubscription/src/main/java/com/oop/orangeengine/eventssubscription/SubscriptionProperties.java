package com.oop.orangeengine.eventssubscription;

import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public class SubscriptionProperties<T extends Event> {

    private long timeOut = -1;
    private int timesToRun = 1;
    private Consumer<SubscribedEvent<T>> onTimeOut;
    private EventPriority priority = EventPriority.NORMAL;
    private boolean async = false;

    public SubscriptionProperties<T> setTimeOut(long timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public SubscriptionProperties<T> setTimeOut(TimeUnit unit, long timeOut) {
        this.timeOut = unit.toMillis(timeOut);
        return this;
    }

    public SubscriptionProperties<T> setTimesToListen(int timesToRun) {
        this.timesToRun = timesToRun;
        return this;
    }

    public SubscriptionProperties<T> onTimeOut(Runnable runnable) {
        return onTimeOut((se) -> runnable.run());
    }

    public SubscriptionProperties<T> onTimeOut(Consumer<SubscribedEvent<T>> onTimeOut) {
        this.onTimeOut = onTimeOut;
        return this;
    }

    public SubscriptionProperties<T> priority(EventPriority priority) {
        this.priority = priority;
        return this;
    }

    public SubscriptionProperties<T> async(boolean async) {
        this.async = async;
        return this;
    }

}
