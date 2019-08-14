package com.oop.orangeengine.main.component;

import com.oop.orangeengine.main.Engine;
import lombok.Getter;

@Getter
public abstract class AEngineComponent implements IEngineComponent {

    private Engine engine;

    public AEngineComponent() {
        engine = Engine.getInstance();
        engine.initComponent(this);
    }
}
