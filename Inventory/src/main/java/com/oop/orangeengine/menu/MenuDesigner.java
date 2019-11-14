package com.oop.orangeengine.menu;

import com.google.common.collect.Sets;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.BukkitItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.oop.orangeengine.main.Engine.getEngine;

public class MenuDesigner implements Cloneable {
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

        for (char[] row : layout) {
            for (char chaz : row) {

                if (chaz == 'A') {
                    menu.addButton(BukkitItem.newAir(realSlot));

                } else {
                    AMenuButton button = buttonMap.get(chaz);
                    if (button == null) {
                        menu.addButton(BukkitItem.newAir(realSlot));

                    } else {
                        menu.addButton(button.clone().slot(realSlot));
                    }
                }

                realSlot++;
            }
        }
    }

    public Set<AMenuButton> getButtons() {
        return Sets.newHashSet(buttonMap.values());
    }

    @Override
    public MenuDesigner clone() {
        MenuDesigner menuDesigner = null;
        try {
            menuDesigner = (MenuDesigner) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assert menuDesigner != null;

        menuDesigner.buttonMap = new HashMap<>();
        MenuDesigner finalMenuDesigner = menuDesigner;

        buttonMap.forEach((key, button) -> finalMenuDesigner.buttonMap.put(key, button.clone()));

        return menuDesigner;
    }
}
