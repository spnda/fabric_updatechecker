package de.sean.updatechecker.mixin;

import de.sean.updatechecker.providers.ModUpdateProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private boolean hasShownUpdateToast = false;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (hasShownUpdateToast || client == null) return;
        hasShownUpdateToast = true;
        if (ModUpdateProvider.availableUpdates.get() == 0) {
            return;
        }

        final var descriptionKey = ModUpdateProvider.availableUpdates.get() == 1
                ? "updatechecker.updatesAvailableToast.description.1"
                : "updatechecker.updatesAvailableToast.description.a";
        SystemToast.add(client.getToastManager(),
                SystemToast.Type.TUTORIAL_HINT,
                Text.translatable("updatechecker.updatesAvailableToast.title"),
                Text.translatable(descriptionKey, ModUpdateProvider.availableUpdates.get()));
    }
}
