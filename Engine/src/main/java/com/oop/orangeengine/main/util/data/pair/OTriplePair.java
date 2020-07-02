package com.oop.orangeengine.main.util.data.pair;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor()
public class OTriplePair<F, S, T> implements IPair {

    private F first;

    private S second;

    private T third;
}
