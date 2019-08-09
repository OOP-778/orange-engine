package com.oop.orangeEngine.main.util.pair;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor()
public class OPair<F, S> implements IPair {

    private F first;
    private S second;

}
