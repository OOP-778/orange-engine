package com.oop.orangeengine.hologram.line;

import com.oop.orangeengine.hologram.HologramLine;
import lombok.NonNull;
import lombok.Setter;

import java.util.function.Supplier;

public class HologramText extends HologramLine<HologramText> {

    @Setter @NonNull
    private Supplier<String> textSupplier;

    // < CACHE >
    private String oldText;

    public HologramText(String text) {
        this.textSupplier = () -> text;
    }

    public HologramText(@NonNull Supplier<String> textSupplier) {
        this.textSupplier = textSupplier;
    }

    public void setText(String text) {
        this.textSupplier = () -> text;
    }

    @Override
    public void update() {
        super.update();
        String newText = textSupplier.get();
        if (oldText != null && newText.hashCode() == oldText.hashCode()) return;

        getWrappedArmorStand().setCustomName(newText);
        getWrappedArmorStand().update();
        oldText = newText;
    }
}
