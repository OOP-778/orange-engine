package com.oop.orangeengine.command;

import java.util.Collection;

public interface TabCompletion {
    Collection<String> handleTabCompletion(ResultCache previousResult, String[] args);
}
