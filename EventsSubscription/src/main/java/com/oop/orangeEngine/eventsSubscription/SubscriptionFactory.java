package com.oop.orangeEngine.eventsSubscription;

import com.oop.orangeEngine.eventsSubscription.subscription.SubEvent;
import com.oop.orangeEngine.eventsSubscription.subscription.SubscribedEvent;
import com.oop.orangeEngine.main.Cleaner;
import com.oop.orangeEngine.main.Engine;
import org.bukkit.event.Event;

import java.util.function.Consumer;

public class SubscriptionFactory {

    private static SubscriptionFactory INSTANCE;

    public SubscriptionFactory() {

        if (INSTANCE != null) {
            try {
                throw new IllegalAccessException("Instance of EventsSubscription class already exists!");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
        }

        INSTANCE = this;
        Engine.getInstance().findComponentByClass(Cleaner.class).registerClass(SubscriptionFactory.class);

    }

    public <T extends Event> SubscribedEvent<T> subscribeTo(Class<T> type, Consumer<T> listener, SubscriptionProperties<T> props) {

        SubscribedEvent<T> subscribedEvent = new SubscribedEvent<>(props);
        SubEvent event = SubEvent.listen(type, props.getPriority(), subscribedEvent::tryHandling, Engine.getInstance().getOwning());

        subscribedEvent.setSubEvent(event);
        subscribedEvent.setListener(listener);
        subscribedEvent.executeTask();

        return subscribedEvent;
    }

    public static SubscriptionFactory getInstance() {
        return INSTANCE;
    }
}
