package com.falsepattern.triangulator.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.triangulator.config.TriConfig;
import com.falsepattern.triangulator.leakfix.LeakFixState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.settings.GameSettings;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(GameSettings.class)
public abstract class GameSettingsOptifineMixin {
    @Shadow(remap = false)
    public int ofChunkLoading;

    @Inject(method = "updateChunkLoading",
            at = @At(value = "HEAD"),
            remap = false,
            require = 1)
    private void blockMultiThreadedChunkLoading(CallbackInfo ci) {
        if (TriConfig.MEMORY_LEAK_FIX != LeakFixState.Disable) {
            ofChunkLoading = 0;
        }
    }
}
