package com.oop.orangeengine.menu;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.BukkitItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                System.out.println("ROW: " + rowNumber + ", SLOT: " + slot);
                layout[rowNumber][slot] = c;
                slot++;
            }

            rowNumber++;
        }
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
                    if (button == null)
                        menu.addButton(BukkitItem.newAir(realSlot));

                    else
                        menu.addButton(button.clone().slot(realSlot));
                }

                realSlot++;

            }
        }
    }
}
