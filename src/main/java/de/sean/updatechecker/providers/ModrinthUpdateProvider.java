package de.sean.updatechecker.providers;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.Util;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModrinthUpdateProvider extends ModUpdateProvider {
    private static final HashMap<String, ModrinthUpdateData> updateData = new HashMap<>();

    private String encodeString(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    @Override
    public void check(String modId, Consumer<AvailableUpdate> callback) {
        if (!updateData.containsKey(modId))
            return;

        beginUpdateCheck();
        Util.getMainWorkerExecutor().execute(() -> {
            final var data = updateData.get(modId);

            var filterParams = new HashMap<String, String>();
            filterParams.put("game_versions", String.format("[\"%s\"]", getGameVersion()));
            filterParams.put("loaders", "[\"fabric\"]");

            var url = filterParams.keySet().stream()
                    .map(key -> key + "=" + encodeString(filterParams.get(key)))
                    .collect(Collectors.joining("&",
                            String.format("https://api.modrinth.com/api/v1/mod/%s/version?", data.projectId),
                            ""));

            var request = new HttpGet(url);
            request.addHeader(HttpHeaders.USER_AGENT, "UpdateChecker (ModrinthUpdateProvider)");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    var entity = response.getEntity();

                    if (entity != null) {
                        var versions = gson.fromJson(EntityUtils.toString(entity), ModrinthVersion[].class);

                        if (versions.length > 0) {
                            var latest = versions[0];

                            if (!latest.versionNumber.equalsIgnoreCase(data.metadata.getVersion().getFriendlyString())) {
                                var update = new AvailableUpdate(
                                        latest.versionNumber,
                                        String.format("https://modrinth.com/mod/%s/version/%s", data.projectId, latest.versionId),
                                        (latest.changeLog == null || latest.changeLog.isEmpty()) ? null : latest.changeLog,
                                        "modrinth"
                                );
                                availableUpdates.incrementAndGet();
                                callback.accept(update);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            completeUpdateCheck();
        });
    }

    @Override
    public void readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject updatesObject) {
        var projectId = getString(updatesObject, "projectId");
        var channel = getString(updatesObject, "channel");

        if (projectId.isEmpty()) {
            throw new RuntimeException("The Modrinth update provider requires a single projectId field.");
        }

        updateData.put(metadata.getId(),
                new ModrinthUpdateData(metadata, modFileName, projectId.get(), channel.orElse("release")));
    }

    public void setFabricUpdateData(@NotNull ModMetadata metadata, String modFileName) {
        updateData.put(metadata.getId(), new ModrinthUpdateData(metadata, modFileName, "P7dR8mSH", "release"));
    }

    public static class ModrinthVersion {
        @SerializedName("id")
        private String versionId;

        @SerializedName("version_number")
        private String versionNumber;

        @SerializedName("changelog")
        private String changeLog;
    }

    public static class ModrinthUpdateData extends ModUpdateData {
        final String projectId;
        final String channel;

        public ModrinthUpdateData(ModMetadata metadata, String modFileName, String projectId, String channel) {
            super(metadata, modFileName);
            this.projectId = projectId;
            this.channel = channel;
        }
    }
}
