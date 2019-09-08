package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.BukkitItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oop.orangeengine.main.Engine.getEngine;

public class MenuDesigner {
    private char[][] layout;
    private Map<Character, AMenuButton> buttonMap = new HashMap<>();

    public MenuDesigner(List<String> rows) {
        layout = new char[rows.size()][9];

        int rowNumber = 0;
        for (String row : rows) {
            int slot = 0;

            char[] chars = row.toCharArray();
            if (chars.length == 0)
                chars = "AAAAAAAAA".toCharArray();

            for (char c : chars) {
                layout[rowNumber][slot] = c;
                slot++;
            }

            rowNumber++;
        }
    }

    public int getSize() {
        return layout.length;
    }

    public MenuDesigner setButton(char layoutId, AMenuButton button) {
        buttonMap.put(layoutId, button);
        return this;
    }

    public void fill(AMenu menu) {
        int realSlot = 0;

        OLogger logger = getEngine().getLogger();
        logger.printWarning("Filling menu!");
        
        for (char[] row : layout) {
            for (char chaz : row) {

                if (chaz == 'A') {
                    menu.addButton(BukkitItem.newAir(realSlot));
                    logger.printWarning("Setting slot: " + realSlot + " char " + chaz + " to AIR");

                } else {
                    AMenuButton button = buttonMap.get(chaz);
                    if (button == null) {
                        menu.addButton(BukkitItem.newAir(realSlot));
                        logger.printWarning("Setting slot: " + realSlot + " char " + chaz + " to AIR");

                    } else {
                        menu.addButton(button.clone().slot(realSlot));
                        logger.printWarning("Setting slot: " + realSlot + " char " + chaz + " to " + button.currentItem().toString());
                    }
                }

                realSlot++;
            }
        }
    }
}
