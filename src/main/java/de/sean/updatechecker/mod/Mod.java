package de.sean.updatechecker.mod;

import de.sean.updatechecker.providers.AvailableUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Mod {
    @NotNull String getId();
    @NotNull String getUpdateProviderId();
    void dispatchUpdateChecks();
    @Nullable AvailableUpdate getAvailableUpdate();
}
