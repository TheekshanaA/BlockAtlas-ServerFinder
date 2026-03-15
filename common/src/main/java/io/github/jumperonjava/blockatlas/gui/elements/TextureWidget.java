package io.github.jumperonjava.blockatlas.gui.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class TextureWidget implements Drawable {
    private Supplier<Identifier> texture;
    private int x;
    private int y;
    private int width;
    private int height;

    public TextureWidget(Supplier<Identifier> texture, int x, int y, int width, int height){
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture.get(),x,y,0,0,width,height,width,height);
    }
}
