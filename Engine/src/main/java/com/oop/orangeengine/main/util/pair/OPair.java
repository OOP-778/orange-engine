package com.oop.orangeengine.main.util.pair;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor()
public class OPair<F, S> implements IPair {

    private F first;
    private S second;

    public F getKey(){
        return first;
    }

    public S getValue() {
        return second;
    }

    public void set(F first, S second) {
        this.first = first;
        this.second = second;
    }

}
