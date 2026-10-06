package com.modgen.orbit_client.mixin;

import com.modgen.orbit_client.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LocalSwingMixin {
    @Inject(method="getHandSwingDuration",at=@At("RETURN"),cancellable=true)
    private void donut$localSwingDuration(CallbackInfoReturnable<Integer> cir) {
        if((Object)this==MinecraftClient.getInstance().player&&Settings.Option.SWING_ENABLED.on()) {
            double multiplier=Settings.Option.SWING.get();
            cir.setReturnValue(Math.max(1,(int)Math.round(cir.getReturnValue()/multiplier)));
        }
    }
}
