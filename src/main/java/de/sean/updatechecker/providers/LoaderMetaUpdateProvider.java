package de.sean.updatechecker.providers;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.Util;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

public class LoaderMetaUpdateProvider extends ModUpdateProvider {
    private static final HashMap<String, LoaderMetaUpdateData> updateData = new HashMap<>();

    @Override
    public void check(String modId, Consumer<AvailableUpdate> callback) {
        if (!updateData.containsKey(modId))
            return;

        beginUpdateCheck();
        Util.getMainWorkerExecutor().execute(() -> {
            final var data = updateData.get(modId);

            var request = new HttpGet(String.format("https://meta.fabricmc.net/v2/versions/loader/%s", getGameVersion()));
            request.addHeader(HttpHeaders.USER_AGENT, "UpdateChecker (LoaderMetaUpdateProvider)");

            try (var response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    var entity = response.getEntity();

                    if (entity != null) {
                        var versions = gson.fromJson(
                                EntityUtils.toString(entity), MetaResponse[].class);

                        for (MetaResponse metaVersion : versions) {
                            var isUpToDate = data.metadata.getVersion().getFriendlyString().equalsIgnoreCase(metaVersion.loader.version);

                            if (!metaVersion.loader.stable && isUpToDate) {
                                // We have a more recent unstable version (beta). Let's not show the update prompt.
                                break;
                            }
                            if (metaVersion.loader.stable && !isUpToDate) {
                                var update = new AvailableUpdate(
                                        metaVersion.loader.version,
                                        "https://fabricmc.net/use/",
                                        null,
                                        "fabricmcNet"
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
    public void readModUpdateData(@NotNull ModMetadata metadata, String modFileName, CustomValue.CvObject updatesObject) {
        updateData.put(metadata.getId(), new LoaderMetaUpdateData(metadata, modFileName));
    }

    private static class MetaResponse {
        private Loader loader;

        private static class Loader {
            private String version;

            private boolean stable;
        }
    }

    public static class LoaderMetaUpdateData extends ModUpdateData {
        public LoaderMetaUpdateData(ModMetadata metadata, String modFileName) {
            super(metadata, modFileName);
        }
    }
}
