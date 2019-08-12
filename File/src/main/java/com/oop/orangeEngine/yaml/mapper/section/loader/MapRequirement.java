package com.oop.orangeEngine.yaml.mapper.section.loader;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MapRequirement {

    public String path;
    public String error_message;
    public Class type;

}
