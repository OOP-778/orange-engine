package com.oop.orangeEngine.command;

import java.util.Collection;

public interface TabCompletion {

    Collection<String> handleTabCompletion(String[] args);

}
