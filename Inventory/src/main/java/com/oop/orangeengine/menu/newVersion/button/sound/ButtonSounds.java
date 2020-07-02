package com.oop.orangeengine.menu.newVersion.button.sound;

import com.oop.orangeengine.sound.OSound;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ButtonSounds {

    private Map<String, OSound> sounds = new HashMap<>();

    public Optional<OSound> get(String id) {
        return Optional.ofNullable(sounds.get(id));
    }

    public ButtonSounds add(String id, OSound sound) {
        sounds.remove(id);
        sounds.put(id, sound);
        return this;
    }
}
