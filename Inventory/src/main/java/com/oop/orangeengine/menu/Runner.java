package com.oop.orangeengine.menu;

import java.util.ArrayList;
import java.util.List;

public class Runner {

    public static void main(String[] args) {

        List<String> rows = new ArrayList<>();
        rows.add("XGXXYXXGX");
        rows.add("");

        MenuDesigner menuDesigner = new MenuDesigner(rows);
    }
}
