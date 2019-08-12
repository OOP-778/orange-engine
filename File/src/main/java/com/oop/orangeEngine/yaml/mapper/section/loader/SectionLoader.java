package com.oop.orangeEngine.yaml.mapper.section.loader;

import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.mapper.SectionMappers;

import java.util.ArrayList;
import java.util.List;

public abstract class SectionLoader<T> {

    public List<MapRequirement> requirementList = new ArrayList<>();
    public Class<T> productClass;

    public SectionLoader(Class<T> productClass) {
        this.productClass = productClass;
    }

    public boolean accepts(ConfigurationSection section) throws IllegalArgumentException {

        for (MapRequirement req : requirementList) {

            //Test Number 1
            if (!section.isPresentValue(req.getPath())) {
                throw new IllegalArgumentException(req.getError_message());
            }

            //Test Number 2
            if (req.type != null && req.type.isInstance(section.getValue(req.path)))
                throw new IllegalArgumentException(req.getError_message());

        }

        return true;

    }

    public void register() {
        SectionMappers.register(this);
    }

    public SectionLoader<T> addRequirement(MapRequirement req) {
        this.requirementList.add(req);
        return this;
    }

    public abstract T map(ConfigurationSection section);

}
