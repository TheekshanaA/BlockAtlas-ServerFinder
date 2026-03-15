package io.github.jumperonjava.blockatlas.api.blockatlas;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.architectury.injectables.targets.ArchitecturyTarget;
import io.github.jumperonjava.blockatlas.api.Server;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

import static io.github.jumperonjava.blockatlas.BlockAtlasInit.LOGGER;
import static io.github.jumperonjava.blockatlas.api.motd.PingWithCache.FAILED_TEXT;

public class BlockAtlasServer implements Server {
    private static String POSTREQUESTLINK = "io.github.jumperonjava.blockatlas.api.blockatlas.BlockAtlasServer&sendPostRequest&";
    private String id;
    private String server_name;
    private int player_count;
    private int max_players;
    private String server_ip;
    private String server_port;
    private List<String> motd;
    private String server_slug;
    private String server_url;
    private String favicon_url;
    private String tags;
    private int rank;
    private boolean featured;

    @Override
    public String id() {
        return id;
    }

    @Override
    public Supplier<Text> motd() {
        return ()-> {
            var motd = Server.super.motd().get();
            if (motd.equals(FAILED_TEXT)) {
                var t = Text.empty();
                if(this.motd==null)
                    return FAILED_TEXT;
                for (var t2 : this.motd) {
                    t2 = t2.replaceAll("§#*[0-9A-F]{6}","");
                    t2 = t2.replaceAll("<#*[0-9A-F]{6}>","");
                    t.append(t2);
                }
                return t;
            }
            return motd;
        };
    }

    @Override
    public String server_name() {
        return server_name;
    }

    @Override
    public int player_count() {
        return player_count;
    }

    @Override
    public int max_players() {
        return max_players;
    }

    @Override
    public String server_ip() {
        return server_ip;
    }

    @Override
    public String server_port() {
        return server_port;
    }

    @Override
    public String server_slug() {
        return server_slug;
    }

    @Override
    public String server_url() {
        return server_url;
    }

    @Override
    public String favicon_url() {
        return favicon_url;
    }

    @Override
    public String tags() {
        return tags;
    }

    @Override
    public int rank() {
        return rank;
    }

    @Override
    public boolean featured() {
        return featured;
    }

    private static ThreadPoolExecutor POST_ASYNC = new ScheduledThreadPoolExecutor(4, (new ThreadFactoryBuilder()).setNameFormat("Record server add #%d").setDaemon(true).build());;
    @Override
    public void onConnected() {
        sendPostRequest(id()+"@ingame");
    }
    public static void sendPostRequest(String arguments){
        POST_ASYNC.submit(()->{
            HttpURLConnection connection = null;
            try {
                var args = arguments.split("@");
                if (args.length < 2) {
                    LOGGER.warn("Invalid post request arguments: {}", arguments);
                    return;
                }
                String url = "https://blockatlas.net/scripts/record_copy_count.php";
                String body = "server_id=" + URLEncoder.encode(args[0], StandardCharsets.UTF_8) + "&method=" + URLEncoder.encode(args[1], StandardCharsets.UTF_8);
                LOGGER.info("Sent record copy count to blockatlas api {}",body);
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String response = br.readLine();
                    LOGGER.info(response);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
    @Override
    public void onAdded() {
        sendPostRequest(id()+"@ingameadd");
    }

    @Override
    public void voteAction() {

    }

    @Override
    public String getVoteLink() {
        return server_url()+"/vote";
    }

    @Override
    public String getPostReq() {
        return POSTREQUESTLINK+id+"@ingame";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockAtlasServer that = (BlockAtlasServer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
