package io.github.jumperonjava.blockatlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jumperonjava.blockatlas.BlockAtlasInit;
import io.github.jumperonjava.blockatlas.api.ListHandler;
import io.github.jumperonjava.blockatlas.api.Server;
import io.github.jumperonjava.blockatlas.api.ServerApi;
import io.github.jumperonjava.blockatlas.api.Tag;
import io.github.jumperonjava.blockatlas.gui.elements.*;
import io.github.jumperonjava.blockatlas.util.ServerInfoExt;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerScreen extends Screen {

    private static int SERVER_LIST_SIZE = 350;
    private static int TAG_LIST_SIZE = 100;
    private static int LIST_GAP = 8;


    private final ServerApi api;
    private final ListHandler<Server> handler = new ServerScreen.Handler(this);
    private final ListHandler<Tag> tagHandler = new TagHandler(this);
    private Runnable loadMore;
    private Runnable tagCallback;
    private ScrollListWidget serverListWidget = new ScrollListWidget(client,100,height-16,8,8,22);
    private final java.util.List<Server> serverList = new ArrayList<>();
    private ScrollListWidget tagListWidget;
    private final java.util.List<Tag> tagList = new ArrayList<>();
    private Runnable activateButtons;
    private Runnable deactivateButtons;

    private Server selectedServer;
    private boolean smallmode=false;
    private Runnable refreshTagCallback;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //renderBackground(context,mouseX,mouseY,delta);
        super.render(context, mouseX, mouseY, delta);
    }

    public ServerScreen(ServerApi serverApi) {
        super(Text.empty());
        this.api = serverApi;
        serverApi.setTagHandler(this.tagHandler);
        serverApi.getMainTag().setServersFromTag(handler);
    }
    public void init(){

        smallmode= 350 + 100 + 8 > width - 10;
        if(smallmode){
            SERVER_LIST_SIZE = 305;
            TAG_LIST_SIZE = 70;
        }
        else {
            SERVER_LIST_SIZE = 350;
            TAG_LIST_SIZE = 100;
        }

        var centerpos = width/2;
        centerpos-=TAG_LIST_SIZE/2;
        centerpos-=SERVER_LIST_SIZE/2;
        centerpos-=LIST_GAP/2;
        final var top = 24;
        final var bottom = 38;

        tagListWidget = new ScrollListWidget(client,TAG_LIST_SIZE,height-bottom,centerpos,top,22);
        //tagCallback.run();
        updateTagList();
        if(!tagListWidget.children().isEmpty())
            tagListWidget.children().get(0).setMeActive();
        api.setTagHandler(tagHandler);
        addDrawable(new ScrollListWidget(client,width,height-bottom,0,top,54));
        serverListWidget = new ScrollListWidget(client,SERVER_LIST_SIZE,height-bottom,centerpos+TAG_LIST_SIZE+LIST_GAP,top,smallmode?38:54);
        updateServerList();
        //title
        addDrawable(((context, mouseX, mouseY, delta) -> context.drawCenteredTextWithShadow(textRenderer,Text.translatable("blockatlas.title"),width/2,8,0xFFFFFFFF)));

        //lower buttons grid
        var listWidth = SERVER_LIST_SIZE+TAG_LIST_SIZE;
        var axis = new AxisGridWidget(width/2-(listWidth)/2,height-bottom+4, listWidth,20, AxisGridWidget.DisplayAxis.HORIZONTAL);
        //buttons themselves
        var buttonSize = (listWidth-44-64)/3-4;
        var directConnect = new ButtonWidget.Builder(Text.translatable("selectServer.select"),(b)->{
            connect(selectedServer);
        }).dimensions(0,0,buttonSize,20).build();
        var addServer = new ButtonWidget.Builder(Text.translatable("selectServer.add"),(b)->{addServer(selectedServer);}).dimensions(0,0,buttonSize,20).build();
        var vote = new ButtonWidget.Builder(Text.translatable("blockatlas.vote"),(b)->{
            try{
                Util.getOperatingSystem().open(new URL(selectedServer.getVoteLink()).toURI());
            }
            catch (Exception e){e.printStackTrace();}
            scheduleUnfocus(b);

        }).dimensions(0,0,buttonSize,20).build();
        var addServerToList = new ButtonWidget.Builder(Text.translatable("blockatlas.addToList"),(b)->{
            try{
                Util.getOperatingSystem().open(new URL("https://blockatlas.net/add-server").toURI());
            }
            catch (Exception e){e.printStackTrace();}
            scheduleUnfocus(b);
        }).dimensions(width/2+listWidth/2-100,2,buttonSize,20).build();
        var close = new ButtonWidget.Builder(Text.translatable("gui.back"),b->{
            this.close();
        }).width(40).build();
        this.deactivateButtons = () ->{
            directConnect.active=false;
            addServer.active=false;
            vote.active=false;
        };
        this.activateButtons = () -> {
            directConnect.active=true;
            addServer.active=true;
            vote.active=true;
        };
        var refresh = new ButtonWidget.Builder(Text.translatable("blockatlas.refresh"),b->{
            this.refresh();
            scheduleUnfocus(b);
        }).width(60).build();
        deactivateButtons.run();
        addDrawableChild(axis.add(directConnect));
        addDrawableChild(axis.add(addServer));
        addDrawableChild(axis.add(vote));
        addDrawableChild(axis.add(refresh));
        addDrawableChild(axis.add(close));
        axis.refreshPositions();

        updateServerList();
        addDrawableChild(tagListWidget);
        addDrawableChild(serverListWidget);
        addDrawableChild((addServerToList));
    }

    private void scheduleUnfocus(ButtonWidget b) {
        new Timer().schedule(new TimerTask(){

            @Override
            public void run() {
                b.setFocused(false);
            }
        },50);
        setFocused(null);
    }

    private void refresh() {
        refreshTagCallback.run();
    }

    private void connect(Server selectedServer) {
        BlockAtlasInit.disconnect();
        var t = (selectedServer.server_ip()+":25565").split(":");
        if (client == null) return;
        ConnectScreen.connect(this,client,new ServerAddress(t[0], Integer.parseInt(t[1])),new ServerInfo("",selectedServer.server_ip(), ServerInfo.ServerType.OTHER),true, null);
        selectedServer.onConnected();
    }

    private void updateTagList() {
        tagListWidget.children().clear();
        api.getTags().forEach(tag -> {
            var e = new ScrollListWidget.ScrollListEntry();
            e.addDrawableChild(new ButtonWidget.Builder(tag.getDisplayName(),b->{tag.setServersFromTag(handler);e.setMeActive();deactivateButtons.run();}).dimensions(0,0,TAG_LIST_SIZE-4,20).build(),true);
            tagListWidget.addEntry(e);
        });
    }
    public void updateServerList(){
        var iconsize = smallmode?32:48;
        RenderSystem.recordRenderCall(()->{
            serverListWidget.children().clear();
            var isLastNull = false;
            for (Server server : new ArrayList<>(serverList)) {
                if(server==null){
                    isLastNull = true;
                    break;
                }
                var e = new ScrollListWidget.ScrollListEntry() {
                };
                if (server.featured()) {
                    e.addDrawable((a, b, c, d) -> a.drawTexture(RenderLayer::getGuiTextured, Identifier.of("blockatlas", "textures/gui/featuredtext.png"), 54 + textRenderer.getWidth(server.server_name()), 5, 0, 0, 51, 7, 51, 7));
                }
                e.addDrawable(new NonCenterTextWidget(iconsize + 4, 4, Text.literal(server.server_name()), textRenderer));
                e.addDrawable(new TextureWidget(new LazyUrlTexture(server.favicon_url()), 2, 2, iconsize, iconsize));

                e.addDrawable((context, mouseX, mouseY, delta) -> {
                    int lineindex = 1;
                    List<OrderedText> list = textRenderer.wrapLines(server.motd().get(), 271);
                    for (var line : list) {
                        context.drawText(textRenderer, line, iconsize + 4, lineindex * 10 + (smallmode ? 6 : 10), 0xFFFFFFFF, false);
                        lineindex++;
                        if (lineindex > 3)
                            break;
                    }
                });
                e.addDrawableChild(new PingIcon(server.server_ip(), SERVER_LIST_SIZE - 5, 2), false);
                var ref = new Object() {
                    long lastClickTime = 1000L;
                };
                e.addDrawableChild(new NonTexturedButton(0, -10, SERVER_LIST_SIZE + 20, 500, Text.empty(), (b) -> {
                    e.setMeActive();
                    selectedServer = server;
                    activateButtons.run();
                    if (Util.getMeasuringTimeMs() - ref.lastClickTime < 250L) {
                        connect(server);
                    }
                    ref.lastClickTime = Util.getMeasuringTimeMs();
                }), false);
                serverListWidget.addEntry(e);
            }
            var e = new ScrollListWidget.ScrollListEntry(){
                protected boolean selectable(){
                    return false;
                }
            };
            if(serverListWidget.children().isEmpty())
                return;
            var loadmore = e.addDrawableChild(new ButtonWidget.Builder(Text.translatable("blockatlas.loadmore"),(b)-> loadMore.run())
                    .dimensions(SERVER_LIST_SIZE/2-50,14,100,20).build(),false);
            serverListWidget.addEntry(e);
            if(isLastNull)
                loadmore.active=false;
        });

    }

    private void addServer(Server server) {
        var serverInfo = new ServerInfo(server.server_name(),server.server_ip(), ServerInfo.ServerType.OTHER);
        ((ServerInfoExt)serverInfo).setVoteLink(server.getVoteLink());
        ((ServerInfoExt)serverInfo).setPostReq(server.getPostReq());

        var servers = new ServerList(client);
        servers.loadFile();
        servers.add(serverInfo, false);
        servers.saveFile();
        close();

        server.onAdded();
    }

    public static class Handler implements ListHandler<Server> {
        private final ServerScreen target;

        public Handler(ServerScreen target){
            this.target = target;
        }

        public void addElement(Server server) {
            target.serverList.add(server);
            target.updateServerList();
        }
        public void tryLoadMoreCallback(Runnable loadMoreCallback) {
            target.loadMore = loadMoreCallback;
        }

        public void refreshCallback(Runnable refreshCallback) {
            target.refreshTagCallback=refreshCallback;
        }

        @Override
        public void clearElements(boolean resetScroll) {
            if(resetScroll)
                target.serverListWidget.setScrollAmount(0);
            target.serverList.clear();
            target.updateServerList();
        }

        @Override
        public void sendError(Throwable error) {
            var e = new ScrollListWidget.ScrollListEntry();
            var lw = target.serverListWidget;
            e.addDrawable((context, mouseX, mouseY, delta) -> {
                if (target.client == null) return;
                context.drawCenteredTextWithShadow(target.client.textRenderer,Text.translatable("blockatlas.servererror"),lw.getRowWidth()/2,4,0xFFFF8888);
                context.drawCenteredTextWithShadow(target.client.textRenderer,Text.literal(error.getMessage()),lw.getRowWidth()/2,14,0xFFFF8888);
            });
            lw.addEntry(e);
        }
    }

    private static class TagHandler implements ListHandler<Tag> {
        private final ServerScreen target;

        public TagHandler(ServerScreen target){
            this.target = target;
        }
        @Override
        public void addElement(Tag tag) {
            target.tagList.add(tag);
            target.updateTagList();
        }

        @Override
        public void tryLoadMoreCallback(Runnable loadMoreCallback) {
            target.tagCallback = loadMoreCallback;
        }

        @Override
        public void clearElements(boolean clearScroll) {
            target.serverListWidget.children().clear();
            target.updateTagList();
        }

        @Override
        public void sendError(Throwable error) {

        }
    }
    public void close() {
        if (client == null) return;
        client.setScreen(new MultiplayerScreen(new TitleScreen()));
    }
}
