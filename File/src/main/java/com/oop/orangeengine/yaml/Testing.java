package com.oop.orangeengine.yaml;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.util.DefaultInitialization;

public class Testing extends AEngineComponent {

    @DefaultInitialization
    public Testing() {

        System.out.println("INITLAIZED");

    }

    @Override
    public String getName() {
        return "testing";
    }
}
