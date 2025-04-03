package io.github.jumperonjava.blockatlas.gui.elements;

import io.github.jumperonjava.blockatlas.api.motd.PingWithCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Collections;
import java.util.List;

public class PingIcon implements Drawable, Element {
    private final String address;
    static final Identifier INCOMPATIBLE_TEXTURE = Identifier.of("server_list/incompatible");
    static final Identifier UNREACHABLE_TEXTURE = Identifier.of("server_list/unreachable");
    static final Identifier PING_1_TEXTURE = Identifier.of("server_list/ping_1");
    static final Identifier PING_2_TEXTURE = Identifier.of("server_list/ping_2");
    static final Identifier PING_3_TEXTURE = Identifier.of("server_list/ping_3");
    static final Identifier PING_4_TEXTURE = Identifier.of("server_list/ping_4");
    static final Identifier PING_5_TEXTURE = Identifier.of("server_list/ping_5");
    static final Identifier PINGING_1_TEXTURE = Identifier.of("server_list/pinging_1");
    static final Identifier PINGING_2_TEXTURE = Identifier.of("server_list/pinging_2");
    static final Identifier PINGING_3_TEXTURE = Identifier.of("server_list/pinging_3");
    static final Identifier PINGING_4_TEXTURE = Identifier.of("server_list/pinging_4");
    static final Identifier PINGING_5_TEXTURE = Identifier.of("server_list/pinging_5");

    static final Text INCOMPATIBLE_TEXT = Text.translatable("multiplayer.status.incompatible");
    static final Text NO_CONNECTION_TEXT = Text.translatable("multiplayer.status.no_connection");
    static final Text PINGING_TEXT = Text.translatable("multiplayer.status.pinging");
    static final Identifier ICONS_TEXTURE = Identifier.of("textures/gui/icons.png");
    private final int x,y;
    private final MinecraftClient client;

    public PingIcon(String server, int x,int y){
        this.address = server;
        this.x = x;
        this.y = y;
        this.client = MinecraftClient.getInstance();
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        var server = PingWithCache.getServer(this.address).orElse(null);

        Text text = server == null ? Text.literal("").formatted(Formatting.RED) : server.playerCountLabel;
        //Text text = bl && false ? server.version.copy().formatted(Formatting.RED) : server.playerCountLabel;
        int j = this.client.textRenderer.getWidth((StringVisitable) text);
        context.drawText(this.client.textRenderer, (Text) text, x - j - 10 - 2, y + 1, 8421504, false);
        Identifier identifier;
        List list2;
        int k;
        Object text2;
        identifier = UNREACHABLE_TEXTURE;
        if (server != null && server.ping != -1L) {
            text2 = Text.translatable("multiplayer.status.ping", new Object[]{server.ping});
            list2 = server.playerListSummary;
            if (server.ping < 150L) {
                identifier = PING_5_TEXTURE;
            } else if (server.ping < 300L) {
                identifier = PING_4_TEXTURE;
            } else if (server.ping < 600L) {
                identifier = PING_3_TEXTURE;
            } else if (server.ping < 1000L) {
                identifier = PING_2_TEXTURE;
            } else {
                identifier = PING_1_TEXTURE;
            }
        } else if (PingWithCache.getting.containsKey(address)) {
            k = 1;
            int l = (int) (Util.getMeasuringTimeMs() / 100L) % 8;
            if (l > 4) {
                l = 8 - l;
            }Identifier var10000;
            switch (k) {
                case 1:
                    var10000 = PINGING_2_TEXTURE;
                    break;
                case 2:
                    var10000 = PINGING_3_TEXTURE;
                    break;
                case 3:
                    var10000 = PINGING_4_TEXTURE;
                    break;
                case 4:
                    var10000 = PINGING_5_TEXTURE;
                    break;
                default:
                    var10000 = PINGING_1_TEXTURE;
            }



            text2 = PINGING_TEXT;
            list2 = Collections.emptyList();
        }//PingWithCache.failed.contains(address)
        else{
                identifier = UNREACHABLE_TEXTURE;
                list2 = Collections.emptyList();
        }

        context.drawGuiTexture(identifier, x - 10, y, 10, 8);
        //context.fill(x,y,x-10,y+8,0xFFFFFF00);
        if(isMouseOver(mouseX,mouseY)){
            var pingtext = Text.literal(String.valueOf(server == null ? "Failed to get ping" : server.ping+" ms"));
            context.drawTooltip(client.textRenderer,pingtext,mouseX-20-client.textRenderer.getWidth(pingtext.getString()),mouseY+12);
        }

    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x-10 && mouseY >= y && mouseX < (double)x && mouseY < y+8;
    }


    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
