package io.github.jumperonjava.blockatlas.api.motd;

import com.google.gson.reflect.TypeToken;
import io.github.jumperonjava.blockatlas.api.Json;
import io.github.jumperonjava.blockatlas.util.FileReadWrite;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PingWithCache {
    public static final Text FAILED_TEXT = Text.translatable("blockatlas.motdfail").setStyle(Style.EMPTY.withColor(TextColor.parse("red")));
    public static final Text LOADING_TEXT = Text.translatable("Loading description...");
    private static Set<String> requiredUpdate = ConcurrentHashMap.newKeySet();
    private static Map<String, ServerInfo> motdCache = new ConcurrentHashMap<>();
    public static final Set<String> failed = ConcurrentHashMap.newKeySet();
    public static final Map<String, Long> getting = new ConcurrentHashMap<>();

    private static File getCacheDirectory() {
        return MinecraftClient.getInstance().getResourcePackDir().resolve("../config").resolve("blockatlas/motdcache.json").toFile();
    }

    static {
        try {
            motdCache = Json.GSON.fromJson(FileReadWrite.read(getCacheDirectory()), new TypeToken<Map<String, ServerInfo>>() {
            }.getType());
            if (motdCache == null)
                motdCache = new ConcurrentHashMap<>();
            motdCache = new ConcurrentHashMap<>(motdCache);
            motdCache.forEach((s, serverInfo) -> requiredUpdate.add(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Text getCachedMotd(String address) {
        if (failed.contains(address)) {
            return FAILED_TEXT;
        }
        var val = getServer(address);
        if (val.isPresent())
            return val.get().label;
        else
            return LOADING_TEXT;
    }

    public static Optional<ServerInfo> getServer(String address) {
        Runnable spa = () -> {
            if (getting.containsKey(address)) {
                var time = getting.getOrDefault(address,0L);
                if(timestamp()-time>10){
                    getting.remove(address);
                    failed.add(address);
                }
                return;
            }
            getting.put(address, timestamp());
            ServerPing.ping(address, PingWithCache::onRecive, () -> {
                failed.add(address);
            });
        };
        if (failed.contains(address))
            return Optional.empty();
        if (motdCache.containsKey(address)) {
            if (requiredUpdate.contains(address)) {
                spa.run();
            }
            return Optional.of(motdCache.get(address));
        } else {
            spa.run();
        }
        return Optional.empty();
    }
    public static long timestamp(){
        return Instant.now().getEpochSecond();
    }

    private static void onRecive(ServerInfo serverInfo) {
        getting.remove(serverInfo.address);
        failed.remove(serverInfo.address);
        requiredUpdate.remove(serverInfo.address);
        motdCache.put(serverInfo.address,serverInfo);
        updateCache();
    }

    private static void updateCache() {
        FileReadWrite.write(getCacheDirectory(),Json.GSON.toJson(motdCache));
    }
}
