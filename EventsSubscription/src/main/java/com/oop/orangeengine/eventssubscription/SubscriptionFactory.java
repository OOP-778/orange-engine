package com.oop.orangeengine.eventssubscription;

import com.oop.orangeengine.eventssubscription.subscription.SubEvent;
import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.component.AEngineComponent;
import org.bukkit.event.Event;

import java.util.function.Consumer;

public class SubscriptionFactory extends AEngineComponent {
    private static SubscriptionFactory INSTANCE;
    static {
        new SubscriptionFactory();
    }

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
    }

    public static SubscriptionFactory getInstance() {
        return INSTANCE;
    }

    public <T extends Event> SubscribedEvent<T> subscribeTo(Class<T> type, Consumer<T> listener, SubscriptionProperties<T> props) {
        SubscribedEvent<T> subscribedEvent = new SubscribedEvent<>(props);
        SubEvent event = SubEvent.listen(type, props.priority(), subscribedEvent::tryHandling, Engine.getInstance().getOwning().getStarter());

        subscribedEvent.subEvent(event);
        subscribedEvent.listener(listener);
        subscribedEvent.executeTask();

        return subscribedEvent;
    }

    @Override
    public String getName() {
        return "Subscription Factory";
    }
}
