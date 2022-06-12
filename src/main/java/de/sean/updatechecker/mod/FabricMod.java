package de.sean.updatechecker.mod;

import de.sean.updatechecker.UpdateChecker;
import de.sean.updatechecker.providers.AvailableUpdate;
import de.sean.updatechecker.providers.ModUpdateProvider;
import de.sean.updatechecker.providers.ModrinthUpdateProvider;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FabricMod implements Mod {
    protected final ModContainer container;
    protected final ModMetadata metadata;
    protected @Nullable AvailableUpdate update;
    private @NotNull String providerId = "";
    private @Nullable CustomValue.CvObject updateData;

    public FabricMod(@NotNull ModContainer container) {
        this.container = container;
        this.metadata = container.getMetadata();

        final var modFileName = FilenameUtils.getBaseName(this.container.getOrigin().toString());

        // Figure out which update provider to use and store the CustomValue object if applicable.
        if (metadata.containsCustomValue("updatedata")) {
            updateData = metadata.getCustomValue("updatedata").getAsObject();
            providerId = updateData.get("provider").getAsString();
        } else {
            if (getId().equals("fabricloader")) {
                providerId = "loader";
            } else if (getId().equals("fabric")) {
                providerId = "modrinth";
            }
        }

        // Read the metadata to figure out the update provider & data
        final var updateProvider = getUpdateProvider();
        updateProvider.ifPresent((provider) -> {
            if (getId().equals("fabricloader")) {
                provider.readModUpdateData(metadata, modFileName, null);
                return;
            } else if (getId().equals("fabric") && provider instanceof ModrinthUpdateProvider modrinthProvider) {
                modrinthProvider.setFabricUpdateData(metadata, modFileName);
                return;
            }

            if (updateData != null)
                provider.readModUpdateData(metadata, modFileName, updateData);
        });
    }

    @Override
    public @NotNull String getId() {
        return metadata.getId();
    }

    @Override
    public @NotNull String getUpdateProviderId() {
        return providerId;
    }

    @Override
    public void dispatchUpdateChecks() {
        getUpdateProvider().ifPresent((provider) -> provider.check(getId(), (update) -> this.update = update));
    }

    private Optional<ModUpdateProvider> getUpdateProvider() {
        return Optional.ofNullable(UpdateChecker.providers.get(getUpdateProviderId()));
    }

    @Override
    public @Nullable AvailableUpdate getAvailableUpdate() {
        return update;
    }
}
