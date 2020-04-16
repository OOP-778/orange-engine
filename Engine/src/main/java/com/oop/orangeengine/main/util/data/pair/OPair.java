package com.oop.orangeengine.main.util.data.pair;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor()
public class OPair<F, S> implements IPair {

    @SerializedName(value = "first")
    private F first;

    @SerializedName(value = "second")
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
