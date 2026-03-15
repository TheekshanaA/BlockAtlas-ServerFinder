package io.github.jumperonjava.blockatlas.gui.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;

public class NonTexturedButton {

    public static ButtonWidget create(int x, int y, int width, int height, MutableText message, ButtonWidget.PressAction onPress) {
        return ButtonWidget.builder(message, onPress).dimensions(x, y, width, height).build();
    }
}
