package com.oop.orangeengine.eventssubscription;

import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class SubscriptionProperties<T extends Event> {

    private long timeOut = -1;
    private int timesToRun = 1;
    private Consumer<SubscribedEvent<T>> onTimeOut;
    private EventPriority priority = EventPriority.NORMAL;
    private boolean async = false;
    private Predicate<T> filter;

    public SubscriptionProperties<T> timeOut(TimeUnit unit, long delay) {
        this.timeOut = unit.toMillis(delay);
        return this;
    }

}
