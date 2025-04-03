package io.github.jumperonjava.blockatlas.gui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;

public class NonCenterTextWidget implements Drawable {
    private final int x;
    private final int y;
    private final Text t;
    private final TextRenderer tr;
    public NonCenterTextWidget(int x, int y, Text text, TextRenderer textRenderer){
        this.x=x;
        this.y=y;
        this.t=text;
        this.tr=textRenderer;
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(tr,t,x,y,0xFFFFFFFF,true);
    }
}
