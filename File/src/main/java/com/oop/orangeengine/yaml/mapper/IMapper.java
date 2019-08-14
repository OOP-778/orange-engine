package com.oop.orangeengine.yaml.mapper;

public interface IMapper<T> {

    T map(String object);

    default T smartMap(Object object){
        return map(object.toString());
    }

    String serialize(T object);

}
