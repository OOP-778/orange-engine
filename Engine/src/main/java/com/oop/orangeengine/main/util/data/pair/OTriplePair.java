package com.oop.orangeengine.main.util.data.pair;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor()
public class OTriplePair<F, S, T> implements IPair {

    @SerializedName(value = "first")
    private F first;

    @SerializedName(value = "second")
    private S second;

    @SerializedName(value = "third")
    private T third;
}
