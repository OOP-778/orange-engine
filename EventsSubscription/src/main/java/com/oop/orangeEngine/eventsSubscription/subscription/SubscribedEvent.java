package com.oop.orangeEngine.eventsSubscription.subscription;

import com.oop.orangeEngine.eventsSubscription.SubscriptionProperties;
import com.oop.orangeEngine.main.Engine;
import com.oop.orangeEngine.main.storage.Storegable;
import com.oop.orangeEngine.main.task.OTask;
import com.oop.orangeEngine.main.task.StaticTask;
import lombok.Setter;
import org.bukkit.event.Event;

import java.util.function.Consumer;

public class SubscribedEvent<T extends Event> extends Storegable {

    private OTask task;
    public SubscribedEvent(SubscriptionProperties<T> props) {
        this.props = props;
    }

    @Setter
    private SubEvent subEvent;

    @Setter
    private Consumer<T> listener;

    private SubscriptionProperties<T> props;
    private int timesRan = 0;

    public void end() {
        SubEvent.unregister(subEvent);
        if(task != null)
            task.cancel();
    }

    public void executeTask() {
        if(props.getTimeOut() != -1)
            task = new OTask().
                    delay(props.getTimeOut()).
                    runnable(() -> {

                        props.getOnTimeOut().accept(this);
                        end();

                    }).
                    execute();
    }

    public void tryHandling(T event) {

        try {

            if(props.isAsync())
                StaticTask.getInstance().async(() -> listener.accept(event));
            else
                StaticTask.getInstance().sync(() -> listener.accept(event));

            timesRan++;
            if(timesRan > 0 && timesRan == props.getTimesToRun())
                end();

        } catch (Exception ex) {
            Engine.getInstance().getLogger().error(ex);
        }

    }

}
