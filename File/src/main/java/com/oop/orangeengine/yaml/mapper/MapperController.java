package com.oop.orangeengine.yaml.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapperController {

    private static final MapperController instance = new MapperController();

    public static MapperController getInstance() {
        return instance;
    }

    private Map<Class, IMapper> mapperMap = new LinkedHashMap<>();

    private MapperController() {

        mapperMap.put(Integer.class, new IMapper<Integer>() {
            @Override
            public Integer map(String object) {
                return Integer.parseInt(object);
            }

            @Override
            public String serialize(Integer object) {
                return "" + object;
            }
        });

    }

}
