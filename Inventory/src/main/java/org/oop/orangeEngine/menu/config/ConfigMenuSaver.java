package org.oop.orangeEngine.menu.config;

import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.OConfiguration;
import com.oop.orangeEngine.yaml.mapper.section.ConfigurationSerializable;
import lombok.AllArgsConstructor;
import org.oop.orangeEngine.menu.AMenu;
import org.oop.orangeEngine.menu.button.AMenuButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class ConfigMenu implements ConfigurationSerializable<AMenu> {

    @Override
    public AMenu load(ConfigurationSection section) {
        return null;
    }

    @Override
    public void save(String path, OConfiguration configuration, AMenu object) {

        ConfigurationSection menuSection = configuration.createNewSection(path);
        menuSection.setValue("_type_", object.getType());

        menuSection.setValue("title", object.getTitle());
        menuSection.setValue("layout", object.getMenuDesigner().getLayout());

        ConfigurationSection buttonsSection = menuSection.createNewSection("buttons");
        for (AMenuButton button : object.getMenuDesigner().getButtonList()) {

            Optional<ConfigurationSerializable> configurationSerializable = Types.find(button.getClass());
            configurationSerializable.ifPresent(s -> s.save(buttonsSection, button));

        }

    }

    public static class Types {
        private static Map<String, ConfigurationSerializable> registeredTypes = new HashMap<>();

        public static <T> void register(String type, ConfigurationSerializable<T> serializable) {
            registeredTypes.put(type, serializable);
        }

        public static Optional<ConfigurationSerializable> find(Class klass) {
            return registeredTypes.values().stream()
                    .filter(s -> s.getJavaClass() == klass)
                    .findFirst();
        }

    }

}
