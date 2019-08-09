package com.oop.testingPlugin;

import com.oop.orangeEngine.eventsSubscription.SubscriptionFactory;
import com.oop.orangeEngine.eventsSubscription.SubscriptionProperties;
import com.oop.orangeEngine.file.OFile;
import com.oop.orangeEngine.main.events.AsyncEvents;
import com.oop.orangeEngine.main.plugin.EnginePlugin;
import com.oop.orangeEngine.message.OMessage;
import com.oop.orangeEngine.message.YamlMessage;
import com.oop.orangeEngine.message.additions.action.CommandAddition;
import com.oop.orangeEngine.message.line.LineContent;
import com.oop.orangeEngine.message.line.MessageLine;
import com.oop.orangeEngine.yaml.OConfiguration;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.TimeUnit;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {

        OFile file = new OFile(getDataFolder(), "messages.yml");
        file.createIfNotExists();

        OConfiguration configuration = new OConfiguration(file.getFile());

        OMessage message = new OMessage();
        MessageLine line = new MessageLine();
        line.append("&c&lHello!");
        line.autoSpaces(true);
        line.append(
                new LineContent("Crazy&eBrian")
                        .hoverText("&cClick &3to go to spawn!")
                        .addAddition(new CommandAddition("run %player%"))
        );

        message.appendLine(line);
        message.appendLine("&aWelcome To Planet 99999999");
        message.setCenter(true);

        YamlMessage.saveToConfig(message, configuration, "test");
        configuration.setValue("testing value", true)
                .addDescription("Testing Description").addDescription("Testing #2")
                .addDescription("Testing #3");

        configuration.appendHeader("Test Plugin Made By Unknown Developer OOF");
        configuration.save();

        OMessage loadedMessage = YamlMessage.fromConfiguration(configuration, "test");
        AsyncEvents.listen(PlayerJoinEvent.class, event -> {

            SubscriptionFactory.getInstance().subscribeTo(
                    PlayerInteractEvent.class,
                    fEvent -> fEvent.getPlayer().sendMessage("Done!"),
                    new SubscriptionProperties<PlayerInteractEvent>()
                            .setTimeOut(TimeUnit.SECONDS, 10)
                            .onTimeOut(() -> event.getPlayer().sendMessage("TimeOut"))
                            .setTimesToListen(5)
            );

        });

    }
}
