package com.oop.orangeengine.eventssubscription;

import com.oop.orangeengine.eventssubscription.subscription.SubEvent;
import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import com.oop.orangeengine.main.Cleaner;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.util.DefaultInitialization;
import org.bukkit.event.Event;

import java.util.function.Consumer;

public class SubscriptionFactory extends AEngineComponent {

    private static SubscriptionFactory INSTANCE;

    @DefaultInitialization
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
        SubEvent event = SubEvent.listen(type, props.priority(), subscribedEvent::tryHandling, Engine.getInstance().getOwning());

        subscribedEvent.setSubEvent(event);
        subscribedEvent.setListener(listener);
        subscribedEvent.executeTask();

        return subscribedEvent;
    }

    public static SubscriptionFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Subscription Factory";
    }
}
