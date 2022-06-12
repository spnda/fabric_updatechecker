package de.sean.updatechecker;

import de.sean.updatechecker.mod.FabricMod;
import de.sean.updatechecker.mod.Mod;
import de.sean.updatechecker.providers.LoaderMetaUpdateProvider;
import de.sean.updatechecker.providers.ModUpdateProvider;
import de.sean.updatechecker.providers.ModrinthUpdateProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.Map;

public class UpdateChecker implements ClientModInitializer {
    final Map<String, Mod> mods = new HashMap<>();

    public static final Map<String, ModUpdateProvider> providers = new HashMap<>();

    @Override
    public void onInitializeClient() {
        providers.put("loader", new LoaderMetaUpdateProvider());
        providers.put("modrinth", new ModrinthUpdateProvider());

        for (var modContainer : FabricLoader.getInstance().getAllMods()) {
            var mod = new FabricMod(modContainer);
            mod.dispatchUpdateChecks();
            mods.put(mod.getId(), mod);
        }
    }
}
