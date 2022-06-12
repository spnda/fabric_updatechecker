package de.sean.updatechecker.providers;

import net.fabricmc.loader.api.metadata.ModMetadata;

public abstract class ModUpdateData {
    public final ModMetadata metadata;
    public final String modFileName;

    public ModUpdateData(ModMetadata metadata, String modFileName) {
        this.metadata = metadata;
        this.modFileName = modFileName;
    }
}
