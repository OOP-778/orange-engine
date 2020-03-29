package com.oop.orangeengine.message.additions.action.secretCmd;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;

public class SecretCommandController {

    private static SecretCommandController instance;
    private Map<String, SecretCommand> secretCommandMap = Maps.newConcurrentMap();

    static {
        new SecretCommandController();
    }

    private SecretCommandController() {
        instance = this;
        SyncEvents.listen(PlayerCommandPreprocessEvent.class, event -> {
            String executed = event.getMessage().replace("/", "");
            SecretCommand secretCommand = secretCommandMap.get(executed);
            if (secretCommand != null) {
                event.setCancelled(true);
                secretCommand.getRun().accept(event.getPlayer());
            }
        });

        // Dispose task
        new OTask()
                .delay(1)
                .repeat(true)
                .sync(false)
                .runnable(() -> secretCommandMap.forEach((id, cmd) -> {
                    if (cmd.getDisposeWhen() != null && cmd.getDisposeWhen().test(cmd))
                        secretCommandMap.remove(id);
                }))
                .execute();
    }

    public void register(String id, SecretCommand command) {
        this.secretCommandMap.put(id, command);
    }

    public static SecretCommandController getInstance() {
        return instance;
    }
}
