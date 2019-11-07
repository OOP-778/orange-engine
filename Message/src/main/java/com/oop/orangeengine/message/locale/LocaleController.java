package com.oop.orangeengine.message.locale;

import com.oop.orangeengine.file.OFile;

import static com.oop.orangeengine.main.Engine.getEngine;

public class LocaleController {

    private static LocaleController localeController;
    public static void load(String locale) {
        OFile file = new OFile(getEngine().getOwning().getDataFolder(), "locale.yml");
        file.createIfNotExists(true);


    }

}
