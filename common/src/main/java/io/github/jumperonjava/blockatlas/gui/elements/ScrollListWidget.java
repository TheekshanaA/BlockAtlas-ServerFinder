package io.github.jumperonjava.blockatlas.gui.elements;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Scroll list widget for general use.
 */
public class ScrollListWidget extends AlwaysSelectedEntryListWidget<ScrollListWidget.ScrollListEntry> {

    public ScrollListWidget(MinecraftClient client, int width, int height, int x, int y, int itemHeight) {
        super(client,width + 6, height - 26, y, itemHeight);
        setX(x - 6);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    public int addEntry(ScrollListEntry entry){
        entry.activationConsumer = this::setSelectedEntry;
        entry.isHoveredFunction = this::isMouseOver;
        entry.currentX+=6;
        return super.addEntry(entry);
    }

    @Override
    protected int getScrollbarX() {
        return getX();
    }



    private ScrollListEntry selectedEntry = new ScrollListEntry();

    public void setSelectedEntry(ScrollListEntry listEntry) {
        selectedEntry.setSelected(false);
        listEntry.setSelected(true);
        selectedEntry=listEntry;
    }

    /**
     * Scroll list entry. Out of box does nothing but using addDrawableChild method you can add widgets for custom behaviour.
     */
    public static class ScrollListEntry extends AlwaysSelectedEntryListWidget.Entry<ScrollListEntry> {
        private final List<Drawable> drawables = Lists.newArrayList();
        private final List<Element> children = Lists.newArrayList();
        private boolean isSelected = false;
        private Consumer<ScrollListEntry> activationConsumer;
        private BiFunction<Integer,Integer,Boolean> isHoveredFunction;
        private final List<Element> deactivate = Lists.newArrayList();

        @Override
        public Text getNarration() {
            return Text.empty();
        }

        int currentX, currentY;

        private void setSelected(boolean selected) {
            this.isSelected = selected;
            for (var d : deactivate) {
                if (d instanceof PressableWidget pw) {
                    pw.active = !isSelected;
                }
            }
        }

        @Override
        public void render(DrawContext context,
                           int mouseX, int mouseY,
                           boolean hovered,
                           float delta) {
            int x = getX();
            int y = getY();
            int entryWidth = getWidth() - 6;
            int entryHeight = getHeight() + 2;
            entryWidth -= 4;

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(x + 6, y);
            if(isSelected){
                context.fill(-1,-1,entryWidth+1,entryHeight+1,0xFFAAAAAA);
                context.fill(0,0,entryWidth,entryHeight,0xFF000000);
            }
            for (var d : drawables) {
                int mx = mouseX;
                int my = mouseY;
                if(isHoveredFunction != null && !isHoveredFunction.apply(mouseX,mouseY)){
                    mx += 100000;
                    my += 100000;
                }
                d.render(context, mx - x - 6, my - y, delta);
                currentX = x + 6;
                currentY = y;
            }
            context.getMatrices().popMatrix();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return super.isMouseOver((mouseX), mouseY) && isHoveredFunction.apply((int) mouseX, (int) mouseY);
        }

        /**
         * adds widget element to this entry.
         * @param drawableElement
         * @param deactivateOnSelect should be this element deactivated when selected. Works only when widget is instance of PressableWidget
         * @return
         * @param <T>
         */
        public <T extends Element & Drawable> T addDrawableChild(T drawableElement, boolean deactivateOnSelect) {
            this.drawables.add(drawableElement);
            this.children.add(drawableElement);
            if (deactivateOnSelect)
                this.deactivate.add(drawableElement);
            return drawableElement;
        }

        public <T extends Drawable> T addDrawable(T drawable){
            this.drawables.add(drawable);
            return drawable;
        }

        protected boolean selectable(){
            return true;
        }

        public void setMeActive() {
            if(!selectable())
                return;
            if (activationConsumer == null)
                return;
            activationConsumer.accept(this);
        }
    }
}