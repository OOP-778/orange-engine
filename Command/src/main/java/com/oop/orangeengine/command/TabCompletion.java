package com.oop.orangeengine.command;

import java.util.Collection;
import java.util.Set;

public interface TabCompletion {
    Collection<String> handleTabCompletion(CompletionResult previousResult, String[] args);
}
