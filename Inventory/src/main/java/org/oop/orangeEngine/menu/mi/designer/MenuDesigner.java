package org.brian.core.mi.designer;

import com.google.common.collect.Iterables;
import org.apache.commons.lang.math.NumberUtils;
import org.brian.core.mi.AMenu;
import org.brian.core.mi.button.AMenuButton;
import org.brian.core.mi.button.DefaultButtons;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * By default character S = none, it will skip that character.
 */

public class MenuDesigner {

    private Map<String, AMenuButton> buttons = new HashMap<>();
    private Map<Row, String> pattern = new HashMap<>();
    private AMenuButton airReplacer;

    private MenuDesigner() {
    }

    public static MenuDesigner create() {
        return new MenuDesigner();
    }

    public MenuDesigner setItem(String character, ItemStack item) {
        buttons.put(character, DefaultButtons.FILLER.getButtonOfItemStack(item));
        return this;
    }


    public MenuDesigner setButton(String character, AMenuButton button) {
        buttons.put(character, button.clone().isFiller(true));
        return this;
    }

    public MenuDesigner setPattern(Row row, String design) {
        if (design.length() > 9 || design.length() < 9) {

            try {
                throw new IllegalStateException(
                        "Incorrect row design size. Required: 9, found: " + design.length());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        pattern.put(row, design);
        return this;
    }

    public MenuDesigner setPattern(int row, String design) {
        if (design.length() > 9 || design.length() < 9) {

            try {
                throw new IllegalStateException(
                        "Incorrect row design size. Required: 9, found: " + design.length());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        pattern.put(new Row(RowType.NUMBER, row), design);
        return this;
    }

    public void applyAsButtons(AMenu menu) {

        int availableRows = menu.size() / 9;

        Map<Integer, Integer> rowSlots = new HashMap<>();
        for (int i = 1; i < availableRows + 1; i++) {
            rowSlots.put(i, (i * 9) - 9);
        }

        int lastStartingSlot = Iterables.getLast(rowSlots.values());
        for (Row row : pattern.keySet()) {

            String rowDesign = pattern.get(row);
            int currentSlot = 0;

            if (row.getType() == RowType.NUMBER) {

                for (Character character : rowDesign.toCharArray()) {
                    int slot = currentSlot;
                    if (row.getRow() != 1)
                        slot = ((row.getRow() - 1) * 9) + currentSlot;
                    if (character.toString().equalsIgnoreCase("S")) {

                        if (airReplacer != null) {

                            AMenuButton button = airReplacer.clone();
                            button.slot(slot);

                            menu.addButton(button);

                        }
                    } else if (buttons.containsKey(character.toString())) {

                        AMenuButton button = buttons.get(character.toString()).clone();
                        button.slot(slot);

                        menu.addButton(button);
                    }
                    currentSlot++;
                }
            } else if (row.getType() == RowType.FIRST) {

                for (Character character : rowDesign.toCharArray()) {
                    if (character.toString().equalsIgnoreCase("S")) {

                        if (airReplacer != null) {

                            AMenuButton button = airReplacer.clone();
                            button.slot(currentSlot);

                            menu.addButton(button);

                        }
                    } else if (buttons.containsKey(character.toString())) {

                        AMenuButton button = buttons.get(character.toString()).clone();
                        button.slot(currentSlot);

                        menu.addButton(button);
                    }
                    currentSlot++;
                }

            } else if (row.getType() == RowType.LAST) {

                currentSlot = lastStartingSlot;

                for (Character character : rowDesign.toCharArray()) {

                    if (character.toString().equalsIgnoreCase("S")) {

                        if (airReplacer != null) {

                            AMenuButton button = airReplacer.clone();
                            button.slot(currentSlot != menu.size() ? currentSlot : currentSlot--);

                            menu.addButton(button);

                        }
                    } else if (buttons.containsKey(character.toString())) {

                        AMenuButton button = buttons.get(character.toString()).clone();
                        button.slot(currentSlot != menu.size() ? currentSlot : currentSlot--);

                        menu.addButton(button);
                    }
                    currentSlot++;
                }

            }
        }
    }


    public Map<Row, String> pattern() {
        return pattern;
    }

    public Map<String, AMenuButton> buttons() {
        return buttons;
    }

    public void decodePattern(List<String> stringList) {

        for (String line : stringList) {

            Row row;
            String[] split = line.split(":");
            if (NumberUtils.isNumber(split[0])) {
                row = new Row(RowType.NUMBER, Integer.parseInt(split[0]));
            } else {
                row = new Row(RowType.valueOf(split[0].toUpperCase()));
            }

            pattern.put(row, split[1]);

        }

    }

    public boolean containsRowType(RowType type) {
        return pattern.keySet().stream().anyMatch(row -> row.getType() == type);
    }

    public void replaceAirWith(AMenuButton button) {

        this.airReplacer = button.clone();
        this.airReplacer.holders().clear();
        this.airReplacer.isTempButton(true);

    }
}
