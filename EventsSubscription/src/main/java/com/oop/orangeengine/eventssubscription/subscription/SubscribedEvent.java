package com.oop.orangeengine.eventssubscription.subscription;

import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.storage.Storegable;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;

import java.util.function.Consumer;

@Accessors(chain = true, fluent = true)
@Getter
public class SubscribedEvent<T extends Event> extends Storegable {

    private OTask task;
    public SubscribedEvent(SubscriptionProperties<T> props) {
        this.props = props;
    }
    private Class<T> type;

    public SubscribedEvent(Class<T> type) {
        this.type = type;
    }

    @Setter
    private SubEvent subEvent;

    @Setter
    private Consumer<T> listener;

    @Setter
    private SubscriptionProperties<T> props;
    private int timesRan = 0;

    public void end() {
        SubEvent.unregister(subEvent);
        if (task != null)
            task.cancel();
    }

    public void executeTask() {
        if (props.timeOut() != -1)
            task = new OTask().
                    delay(props.timeOut()).
                    runnable(() -> {

                        props.onTimeOut().accept(this);
                        end();

                    }).
                    execute();
    }

    public void tryHandling(T event) {
        try {
            if (props.filter() != null && !props.filter().test(event))
                return;

            if (props.async())
                StaticTask.getInstance().async(() -> listener.accept(event));
            else
                StaticTask.getInstance().sync(() -> listener.accept(event));

            timesRan++;
            if (props.runTill() != null) {
                if (props.runTill().test(event))
                    end();
            } else if (timesRan > 0 && timesRan == props.timesToRun())
                end();

        } catch (Exception ex) {
            Engine.getInstance().getLogger().error(ex);
        }

    }

    public void subscribe() {
        SubscriptionFactory.getInstance().subscribeTo(this);
    }

}
