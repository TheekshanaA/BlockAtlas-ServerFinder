package io.github.jumperonjava.blockatlas;

import io.github.jumperonjava.blockatlas.api.ServerApi;
import io.github.jumperonjava.blockatlas.api.blockatlas.BlockAtlasApi;
import io.github.jumperonjava.blockatlas.api.motd.PingWithCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BlockAtlasInit{
    public static ServerApi api;

    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static final Logger LOGGER = LoggerFactory.getLogger("BlockAtlas");

    public static void disconnect() {
        boolean bl = client.isInSingleplayer();

        if(client.world!=null)
            client.world.disconnect(Text.translatable("menu.disconnect"));

        if (bl)
            client.disconnect(Text.translatable("menu.savingLevel"));
         else
            client.disconnect(Text.translatable("menu.disconnect"));


        TitleScreen titleScreen = new TitleScreen();

        if (bl)
            client.setScreen(titleScreen);
        else
            client.setScreen(new MultiplayerScreen(titleScreen));


    }
    public static void initApi(){
        if(api!=null) return;

        api = new BlockAtlasApi();
        new PingWithCache();
    }
}
