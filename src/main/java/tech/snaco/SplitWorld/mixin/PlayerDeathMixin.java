package tech.snaco.SplitWorld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import tech.snaco.SplitWorld.PlayerDeathCallback;

@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin {
    @Inject(at = @At("HEAD"), method="Lnet/minecraft/server/network/ServerPlayerEntity;onDeath()V", cancellable = true)
    private void onDeath(final DamageSource damageSource, final CallbackInfo info) {
        ActionResult result = PlayerDeathCallback.EVENT.invoker().interact((ServerPlayerEntity) (Object) (this));
        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
