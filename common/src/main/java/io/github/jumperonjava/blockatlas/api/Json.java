package io.github.jumperonjava.blockatlas.api;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class Json {
    public static final Gson GSON;
    static {
        GSON = new GsonBuilder().registerTypeAdapter(Text.class, new TextTypeAdapter()).setPrettyPrinting().create();
    }
    private static final ThreadPoolExecutor GET_JSON_ASYNC = new ScheduledThreadPoolExecutor(4, (new ThreadFactoryBuilder()).setNameFormat("Json getter pool %d").setDaemon(true).build());;
    public static void getFromUrl(String urlString, Consumer<String> onSuccess,Runnable onFail)
    {
        GET_JSON_ASYNC.submit(()-> {
            String json = null;
            try {
                URL url = new URL(urlString);
                url.openConnection();
                InputStream is = url.openStream();
                json = new String(is.readAllBytes());
                is.close();
            } catch (IOException e) {
                onFail.run();
            }
            if(json == null)
            {
                new RuntimeException("No Json Response").printStackTrace();
                onFail.run();
            }
            if(Objects.equals(json, ""))
            {
                new RuntimeException("Empty Json Response").printStackTrace();
                onFail.run();
                return;
            }
            onSuccess.accept(json);
        });
    }
}
