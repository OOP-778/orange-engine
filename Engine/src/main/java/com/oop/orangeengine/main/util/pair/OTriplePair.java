package com.oop.orangeengine.main.util.pair;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class OTriplePair<F, S, T> implements IPair {

    private F first;
    private S second;
    private T third;

}
