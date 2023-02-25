package nl.enjarai.rites.mixin;

import net.minecraft.server.DataPackContents;
import nl.enjarai.rites.resource.CircleTypes;
import nl.enjarai.rites.resource.Rituals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("FinalizeCalledExplicitly")
@Mixin(DataPackContents.class)
public abstract class DataPackContentsMixin {
    @Inject(
            method = "refresh",
            at = @At("TAIL")
    )
    private void rites$afterTagsLoaded(CallbackInfo ci) {
        CircleTypes.INSTANCE.finalize();
        Rituals.INSTANCE.finalize();
    }
}
