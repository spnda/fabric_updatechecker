package de.sean.updatechecker.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.MinecraftVersion;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class ModUpdateProvider {
    public static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    public static final Gson gson = new GsonBuilder().create();

    public static final AtomicInteger availableUpdates = new AtomicInteger();
    private static final AtomicInteger runningChecks = new AtomicInteger();

    public static void beginUpdateCheck() {
        runningChecks.incrementAndGet();
    }

    public static void completeUpdateCheck() {
        if (runningChecks.decrementAndGet() == 0) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getGameVersion() {
        return MinecraftVersion.CURRENT.getName();
    }

    public static Optional<String> getString(CustomValue.@NotNull CvObject metadata, String key) {
        if (metadata.containsKey(key)) {
            return Optional.of(metadata.get(key).getAsString());
        }
        return Optional.empty();
    }

    public abstract void check(String modId, Consumer<AvailableUpdate> callback);
    public abstract void readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject updatesObject);
}
