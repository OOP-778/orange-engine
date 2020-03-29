package com.oop.orangeengine.database.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class Took {

    private Instant started;
    private Instant ended;

    private Took(Instant started) {
        this.started = started;
    }

    public static Took now() {
        return new Took(Instant.now());
    }

    public long end() {
        this.ended = Instant.now();
        return Duration.between(started, ended).toMillis();
    }

    public void restart() {
        this.started = Instant.now();
        this.ended = null;
    }

    public long took() {
        Objects.requireNonNull(started);
        Objects.requireNonNull(ended);

        return Duration.between(started, ended).toMillis();
    }

}
