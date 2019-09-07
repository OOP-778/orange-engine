package com.oop.orangeengine.menu.config;

import com.oop.orangeengine.menu.config.button.AConfigButton;

import java.util.ArrayList;
import java.util.List;

public class ConfigMenuTemplate {

    private MenuType menuType;
    private String title;

    private List<String> layout = new ArrayList<>();
    private List<AConfigButton> buttons = new ArrayList<>();

    private ConfigMenuTemplate parent;
    private List<ConfigMenuTemplate> children = new ArrayList<>();

}
