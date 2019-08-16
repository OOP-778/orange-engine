package com.oop.orangeengine.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class WrappedSound {

    private OSound sound;
    private float pitch;
    private float volume;

    public void play(Location location) {
        sound.play(location, volume, pitch);
    }

    public void play(Player player) {
        sound.play(player, volume, pitch);
    }

}
